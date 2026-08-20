package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.mentor.MentorDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.mentor.CreateMentorForm;
import com.ai.api.jwt.BaseJwt;
import com.ai.api.mapper.MentorMapper;
import com.ai.api.mapper.MentorMapperImpl;
import com.ai.api.mapper.AccountMapperImpl;
import com.ai.api.model.Account;
import com.ai.api.model.Group;
import com.ai.api.model.Mentor;
import com.ai.api.model.criteria.MentorCriteria;
import com.ai.api.repository.AccountRepository;
import com.ai.api.repository.GroupRepository;
import com.ai.api.repository.MentorRepository;
import com.ai.api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link MentorController#create}, written FIRST against the planned
 * `POST /v1/mentor/create` shape (FR-002) — {@code MentorController} does not exist yet on
 * disk, so this class is expected to fail to compile/run until it is created (red state).
 *
 * <p>Note on error-code assertions: a dedicated {@code ErrorCode.MENTOR_ERROR_GROUP_KIND_INVALID}
 * (or equivalent) is not registered yet either (that lands with the controller in a later task),
 * so cases (b)/(c)/(d) assert on the thrown exception type ({@link BadRequestException}) rather
 * than hard-depending on a not-yet-existing {@code ErrorCode} constant name.
 */
@ExtendWith(MockitoExtension.class)
class MentorControllerTest {

    @Mock
    private MentorRepository mentorRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private MentorMapper mentorMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private MentorController mentorController;

    private BaseJwt jwtFor(long accountId) {
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(accountId);
        return jwt;
    }

    private CreateMentorForm createForm(String username, String email, String phone,
                                         String password, String fullName, Long groupId,
                                         String position, String description) {
        CreateMentorForm form = new CreateMentorForm();
        form.setUsername(username);
        form.setEmail(email);
        form.setPhone(phone);
        form.setPassword(password);
        form.setFullName(fullName);
        form.setGroupId(groupId);
        form.setPosition(position);
        form.setDescription(description);
        return form;
    }

    // ------------------------------------------------------------------ (a) happy path

    @Test
    void shouldCreateAccountAndMentorWhenGroupIsMentorKindAndUnique() {
        // Arrange
        CreateMentorForm form = createForm("mentor1", "mentor1@example.com", "0912345678",
                "Password1!", "Mentor One", 1L, "Senior Mentor", "Experienced mentor");

        Group group = new Group();
        group.setId(1L);
        group.setKind(AIConstant.GROUP_KIND_MENTOR);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(accountRepository.existsByUsername("mentor1")).thenReturn(false);
        when(accountRepository.existsByEmailAndStatusNot("mentor1@example.com", AIConstant.STATUS_DELETE))
                .thenReturn(false);
        when(accountRepository.existsByPhoneAndStatusNot("0912345678", AIConstant.STATUS_DELETE))
                .thenReturn(false);

        Account mappedAccount = new Account();
        when(mentorMapper.fromFormToAccount(form)).thenReturn(mappedAccount);
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded-password");

        Mentor mappedMentor = new Mentor();
        when(mentorMapper.fromFormToEntity(form)).thenReturn(mappedMentor);

        BindingResult bindingResult = mock(BindingResult.class);

        // Act
        ApiMessageDto<MentorDto> result = mentorController.create(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();

        assertThat(mappedAccount.getKind()).isEqualTo(AIConstant.USER_KIND_MENTOR);
        assertThat(mappedAccount.getPassword()).isEqualTo("encoded-password");
        assertThat(mappedAccount.getGroup()).isEqualTo(group);
        verify(accountRepository).save(mappedAccount);

        assertThat(mappedMentor.getAccount()).isEqualTo(mappedAccount);
        verify(mentorRepository).save(mappedMentor);
    }

    // ------------------------------------------------------------- (b) wrong group kind

    @Test
    void shouldThrowBadRequestWhenGroupKindIsNotMentor() {
        // Arrange
        CreateMentorForm form = createForm("mentor1", "mentor1@example.com", "0912345678",
                "Password1!", "Mentor One", 1L, "Senior Mentor", "Experienced mentor");

        Group group = new Group();
        group.setId(1L);
        group.setKind(AIConstant.GROUP_KIND_ADMIN);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        BindingResult bindingResult = mock(BindingResult.class);

        // Act + Assert
        assertThatThrownBy(() -> mentorController.create(form, bindingResult))
                .isInstanceOf(BadRequestException.class);
        verify(accountRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(mentorRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    // -------------------------------------------------------------- (c) group not found

    @Test
    void shouldThrowBadRequestWhenGroupNotFound() {
        // Arrange
        CreateMentorForm form = createForm("mentor1", "mentor1@example.com", "0912345678",
                "Password1!", "Mentor One", 99L, "Senior Mentor", "Experienced mentor");

        when(groupRepository.findById(99L)).thenReturn(Optional.empty());

        BindingResult bindingResult = mock(BindingResult.class);

        // Act + Assert
        assertThatThrownBy(() -> mentorController.create(form, bindingResult))
                .isInstanceOf(BadRequestException.class);
        verify(accountRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(mentorRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    // ------------------------------------------------------------- (d) duplicate username

    @Test
    void shouldThrowBadRequestWhenUsernameAlreadyExists() {
        // Arrange
        CreateMentorForm form = createForm("mentor1", "mentor1@example.com", "0912345678",
                "Password1!", "Mentor One", 1L, "Senior Mentor", "Experienced mentor");

        Group group = new Group();
        group.setId(1L);
        group.setKind(AIConstant.GROUP_KIND_MENTOR);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(accountRepository.existsByUsername("mentor1")).thenReturn(true);

        BindingResult bindingResult = mock(BindingResult.class);

        // Act + Assert
        assertThatThrownBy(() -> mentorController.create(form, bindingResult))
                .isInstanceOf(BadRequestException.class);
        verify(accountRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(mentorRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    // ---------------------------------------------------------- (e) auto-complete shape (FR-007)

    @Test
    void shouldReturnAutoCompleteShapeWithAccountUsernameEmailPhoneGroupNullWhenAutoComplete() {
        // Arrange - wire up the REAL generated mapper chain (MentorMapperImpl -> AccountMapperImpl)
        // so this test proves the actual @Named delegation used by the controller, not a stub.
        MentorMapperImpl realMentorMapper = new MentorMapperImpl();
        AccountMapperImpl realAccountMapper = new AccountMapperImpl();
        ReflectionTestUtils.setField(realMentorMapper, "accountMapper", realAccountMapper);

        MentorController controller = new MentorController();
        ReflectionTestUtils.setField(controller, "mentorRepository", mentorRepository);
        ReflectionTestUtils.setField(controller, "mentorMapper", realMentorMapper);

        Group group = new Group();
        group.setId(1L);

        Account account = new Account();
        account.setId(10L);
        account.setUsername("mentor1");
        account.setEmail("mentor1@example.com");
        account.setPhone("0912345678");
        account.setFullName("Mentor One");
        account.setAvatarPath("/avatar/mentor1.png");
        account.setGroup(group);

        Mentor mentor = new Mentor();
        mentor.setId(100L);
        mentor.setPosition("Senior Mentor");
        mentor.setDescription("Experienced mentor");
        mentor.setAccount(account);

        MentorCriteria criteria = new MentorCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Mentor> page = new PageImpl<>(Collections.singletonList(mentor));

        when(mentorRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // Act
        ApiMessageDto<ResponseListDto<List<MentorDto>>> result = controller.autoComplete(criteria, pageable);

        // Assert
        MentorDto mentorDto = result.getData().getContent().get(0);
        assertThat(mentorDto.getAccount().getUsername()).isNull();
        assertThat(mentorDto.getAccount().getEmail()).isNull();
        assertThat(mentorDto.getAccount().getPhone()).isNull();
        assertThat(mentorDto.getAccount().getGroup()).isNull();

        assertThat(mentorDto.getAccount().getId()).isEqualTo(10L);
        assertThat(mentorDto.getAccount().getFullName()).isEqualTo("Mentor One");
        assertThat(mentorDto.getAccount().getAvatarPath()).isEqualTo("/avatar/mentor1.png");

        assertThat(mentorDto.getPosition()).isNull();
        assertThat(mentorDto.getDescription()).isNull();

        assertThat(mentorDto.getStatus()).isNull();
        assertThat(mentorDto.getCreatedDate()).isNull();
        assertThat(mentorDto.getModifiedDate()).isNull();
    }

    // ------------------------------------------------------------------ (f)/(g) profile (FR-009)

    @Test
    void shouldReturnCurrentMentorProfileWhenFound() {
        // Arrange
        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));

        Mentor mentor = new Mentor();
        mentor.setId(1L);

        MentorDto mentorDto = new MentorDto();

        when(mentorRepository.findByIdAndStatus(1L, AIConstant.STATUS_ACTIVE)).thenReturn(Optional.of(mentor));
        when(mentorMapper.fromEntityToMentorDto(mentor)).thenReturn(mentorDto);

        // Act
        ApiMessageDto<MentorDto> result = mentorController.profile();

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData()).isEqualTo(mentorDto);
    }

    @Test
    void shouldThrowNotFoundWhenProfileMentorNotFound() {
        // Arrange
        when(userService.getAddInfoFromToken()).thenReturn(jwtFor(1L));

        when(mentorRepository.findByIdAndStatus(1L, AIConstant.STATUS_ACTIVE)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> mentorController.profile())
                .isInstanceOf(NotFoundException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.MENTOR_ERROR_NOT_FOUND);
    }
}
