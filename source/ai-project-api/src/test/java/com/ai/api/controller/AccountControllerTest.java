package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.account.AccountDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.UnauthorizationException;
import com.ai.api.form.account.CreateAccountAdminForm;
import com.ai.api.form.account.UpdateAccountAdminForm;
import com.ai.api.form.account.UpdateProfileAdminForm;
import com.ai.api.jwt.BaseJwt;
import com.ai.api.mapper.AccountMapper;
import com.ai.api.model.Account;
import com.ai.api.model.Group;
import com.ai.api.repository.AccountRepository;
import com.ai.api.repository.GroupRepository;
import com.ai.api.service.BaseApiService;
import com.ai.api.service.FileService;
import com.ai.api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link AccountController}, scoped to the avatar-file-cleanup behavior added on
 * update-admin/update-profile-admin/delete (this project's first companion test for
 * AccountController — the rest of its endpoints are pre-existing and out of scope here).
 */
@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private GroupRepository groupRepository;

    @Spy
    private AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    @Mock
    private BaseApiService baseApiService;

    @Mock
    private FileService fileService;

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private AccountController accountController;

    private BaseJwt superAdminJwt(long accountId) {
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(accountId);
        jwt.setIsSuperAdmin(true);
        return jwt;
    }

    // --------------------------------------------------------- create-admin

    @Test
    void shouldCreateAdminAccountAndReturnIdWhenCallerIsSuperAdmin() {
        // Arrange
        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt(1L));
        CreateAccountAdminForm form = new CreateAccountAdminForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        form.setUsername("admin1");
        form.setFullName("Admin One");
        form.setPassword("Password1!");
        form.setGroupId(2L);
        Group group = new Group();
        group.setId(2L);

        when(groupRepository.findById(2L)).thenReturn(Optional.of(group));
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded-password");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account saved = invocation.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        // Act
        ApiMessageDto<AccountDto> result = accountController.createAdmin(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getId()).isEqualTo(5L);
        assertThat(result.getMessage()).isEqualTo("Create account admin success");
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        Account saved = accountCaptor.getValue();
        assertThat(saved.getUsername()).isEqualTo("admin1");
        assertThat(saved.getFullName()).isEqualTo("Admin One");
        assertThat(saved.getKind()).isEqualTo(AIConstant.USER_KIND_ADMIN);
        assertThat(saved.getPassword()).isEqualTo("encoded-password");
        assertThat(saved.getGroup()).isSameAs(group);
    }

    @Test
    void shouldThrowUnauthorizationWhenCreateAdminCallerIsNotSuperAdmin() {
        // Arrange
        CreateAccountAdminForm form = new CreateAccountAdminForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(1L);
        jwt.setIsSuperAdmin(false);
        when(userService.getAddInfoFromToken()).thenReturn(jwt);

        // Act + Assert
        assertThatThrownBy(() -> accountController.createAdmin(form, bindingResult))
                .isInstanceOf(UnauthorizationException.class);
        verify(accountRepository, never()).save(any());
    }

    // --------------------------------------------------------- update-admin

    @Test
    void shouldDeleteOldAvatarWhenUpdateAdminAvatarPathChanges() {
        // Arrange
        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt(1L));
        UpdateAccountAdminForm form = new UpdateAccountAdminForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        form.setId(1L);
        form.setGroupId(1L);
        form.setAvatarPath("/avatar/new.png");

        Account account = new Account();
        account.setId(1L);
        account.setAvatarPath("/avatar/old.png");
        Group group = new Group();
        group.setId(1L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        // Act
        ApiMessageDto<Void> result = accountController.updateAdmin(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(fileService).deleteFile("/avatar/old.png");
        verify(accountRepository).save(account);
        assertThat(account.getAvatarPath()).isEqualTo("/avatar/new.png");
    }

    @Test
    void shouldNotDeleteOldAvatarWhenUpdateAdminAvatarPathIsUnchanged() {
        // Arrange
        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt(1L));
        UpdateAccountAdminForm form = new UpdateAccountAdminForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        form.setId(1L);
        form.setGroupId(1L);
        form.setAvatarPath("/avatar/same.png");

        Account account = new Account();
        account.setId(1L);
        account.setAvatarPath("/avatar/same.png");
        Group group = new Group();
        group.setId(1L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        // Act
        accountController.updateAdmin(form, bindingResult);

        // Assert
        verify(fileService, never()).deleteFile(any());
    }

    @Test
    void shouldDeleteOldAvatarWhenUpdateAdminClearsAvatarPathToNull() {
        // Arrange
        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt(1L));
        UpdateAccountAdminForm form = new UpdateAccountAdminForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        form.setId(1L);
        form.setGroupId(1L);
        form.setAvatarPath(null);

        Account account = new Account();
        account.setId(1L);
        account.setAvatarPath("/avatar/old.png");
        Group group = new Group();
        group.setId(1L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        // Act
        accountController.updateAdmin(form, bindingResult);

        // Assert
        verify(fileService).deleteFile("/avatar/old.png");
        assertThat(account.getAvatarPath()).isNull();
    }

    @Test
    void shouldThrowUnauthorizedWhenUpdateAdminCallerIsNotSuperAdmin() {
        // Arrange
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(1L);
        jwt.setIsSuperAdmin(false);
        when(userService.getAddInfoFromToken()).thenReturn(jwt);
        UpdateAccountAdminForm form = new UpdateAccountAdminForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        form.setId(1L);

        // Act + Assert
        assertThatThrownBy(() -> accountController.updateAdmin(form, bindingResult))
                .isInstanceOf(UnauthorizationException.class);
        verify(fileService, never()).deleteFile(any());
    }

    // --------------------------------------------------- update-profile-admin

    @Test
    void shouldDeleteOldAvatarWhenUpdateProfileAdminAvatarPathChanges() {
        // Arrange
        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt(1L));
        UpdateProfileAdminForm form = new UpdateProfileAdminForm();
        form.setFullName("Admin One");
        form.setAvatarPath("/avatar/new.png");
        form.setOldPassword("current-password");

        Account account = new Account();
        account.setId(1L);
        account.setAvatarPath("/avatar/old.png");
        account.setPassword("encoded-current-password");

        when(accountRepository.findByIdAndStatus(1L, AIConstant.STATUS_ACTIVE))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches("current-password", "encoded-current-password")).thenReturn(true);

        // Act
        ApiMessageDto<Void> result = accountController.updateProfileAdmin(form, new BeanPropertyBindingResult(form, "form"));

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(fileService).deleteFile("/avatar/old.png");
        assertThat(account.getAvatarPath()).isEqualTo("/avatar/new.png");
    }

    @Test
    void shouldNotDeleteOldAvatarWhenUpdateProfileAdminAvatarPathIsUnchanged() {
        // Arrange
        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt(1L));
        UpdateProfileAdminForm form = new UpdateProfileAdminForm();
        form.setFullName("Admin One");
        form.setAvatarPath("/avatar/same.png");
        form.setOldPassword("current-password");

        Account account = new Account();
        account.setId(1L);
        account.setAvatarPath("/avatar/same.png");
        account.setPassword("encoded-current-password");

        when(accountRepository.findByIdAndStatus(1L, AIConstant.STATUS_ACTIVE))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches("current-password", "encoded-current-password")).thenReturn(true);

        // Act
        accountController.updateProfileAdmin(form, new BeanPropertyBindingResult(form, "form"));

        // Assert
        verify(fileService, never()).deleteFile(any());
    }

    @Test
    void shouldDeleteOldAvatarWhenUpdateProfileAdminClearsAvatarPathToNull() {
        // Arrange
        when(userService.getAddInfoFromToken()).thenReturn(superAdminJwt(1L));
        UpdateProfileAdminForm form = new UpdateProfileAdminForm();
        form.setFullName("Admin One");
        form.setAvatarPath(null);
        form.setOldPassword("current-password");

        Account account = new Account();
        account.setId(1L);
        account.setAvatarPath("/avatar/old.png");
        account.setPassword("encoded-current-password");

        when(accountRepository.findByIdAndStatus(1L, AIConstant.STATUS_ACTIVE))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches("current-password", "encoded-current-password")).thenReturn(true);

        // Act
        accountController.updateProfileAdmin(form, new BeanPropertyBindingResult(form, "form"));

        // Assert
        verify(fileService).deleteFile("/avatar/old.png");
        assertThat(account.getAvatarPath()).isNull();
    }

    // ------------------------------------------------------------------ delete

    @Test
    void shouldDeleteAvatarFileWhenDeletingAccountWithNonBlankAvatarPath() {
        // Arrange
        Account account = new Account();
        account.setId(1L);
        account.setAvatarPath("/avatar/to-delete.png");
        account.setIsSuperAdmin(false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // Act
        ApiMessageDto<Void> result = accountController.delete(1L);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(fileService).deleteFile("/avatar/to-delete.png");
        verify(accountRepository).deleteById(1L);
    }

    @Test
    void shouldNotDeleteFileWhenDeletingAccountWithBlankAvatarPath() {
        // Arrange
        Account account = new Account();
        account.setId(1L);
        account.setIsSuperAdmin(false);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // Act
        accountController.delete(1L);

        // Assert
        verify(fileService, never()).deleteFile(any());
        verify(accountRepository).deleteById(1L);
    }

    @Test
    void shouldThrowBadRequestWhenDeletingSuperAdminAccount() {
        // Arrange
        Account account = new Account();
        account.setId(1L);
        account.setIsSuperAdmin(true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // Act + Assert
        assertThatThrownBy(() -> accountController.delete(1L))
                .isInstanceOf(BadRequestException.class);
        verify(fileService, never()).deleteFile(any());
        verify(accountRepository, never()).deleteById(any());
    }
}
