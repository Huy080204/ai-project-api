package com.ai.api.controller;

import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.company.CompanyDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.company.CreateCompanyForm;
import com.ai.api.form.company.UpdateCompanyForm;
import com.ai.api.mapper.CompanyMapper;
import com.ai.api.model.Company;
import com.ai.api.repository.CompanyRepository;
import com.ai.api.repository.JobPostingRepository;
import com.ai.api.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link CompanyController}. CompanyController does not exist yet (batch 4) — this
 * test is written against its intended API and is expected to fail to compile until then.
 */
@ExtendWith(MockitoExtension.class)
class CompanyControllerTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private FileService fileService;

    @Mock
    private JobPostingRepository jobPostingRepository;

    @InjectMocks
    private CompanyController companyController;

    // ------------------------------------------------------------------ create

    @Test
    void shouldThrowBadRequestWhenCreateNameAlreadyExists() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        CreateCompanyForm form = new CreateCompanyForm();
        form.setName("Company One");
        form.setAvatar("/avatar/new.png");

        when(companyRepository.existsByName(anyString())).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> companyController.create(form, bindingResult))
                .isInstanceOf(BadRequestException.class);
        verify(companyRepository, never()).save(any());
    }

    @Test
    void shouldCreateCompanySuccessfully() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        CreateCompanyForm form = new CreateCompanyForm();
        form.setName("Company One");
        form.setAvatar("/avatar/new.png");

        Company company = new Company();
        company.setName("Company One");
        company.setAvatar("/avatar/new.png");

        when(companyRepository.existsByName(anyString())).thenReturn(false);
        when(companyMapper.fromFormToEntity(form)).thenReturn(company);

        // Act
        ApiMessageDto<CompanyDto> result = companyController.create(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(companyRepository).save(company);
    }

    // ------------------------------------------------------------------ update

    @Test
    void shouldThrowNotFoundWhenUpdateIdMissing() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateCompanyForm form = new UpdateCompanyForm();
        form.setId(1L);
        form.setName("Company One");
        form.setAvatar("/avatar/new.png");

        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> companyController.update(form, bindingResult))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowBadRequestWhenUpdateNameExistsOnAnotherCompany() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateCompanyForm form = new UpdateCompanyForm();
        form.setId(1L);
        form.setName("Company Two");
        form.setAvatar("/avatar/same.png");

        Company company = new Company();
        company.setId(1L);
        company.setName("Company One");
        company.setAvatar("/avatar/same.png");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.existsByName(anyString())).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> companyController.update(form, bindingResult))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldDeleteOldAvatarWhenUpdateAvatarChanges() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateCompanyForm form = new UpdateCompanyForm();
        form.setId(1L);
        form.setName("Company One");
        form.setAvatar("/new.png");

        Company company = new Company();
        company.setId(1L);
        company.setName("Company One");
        company.setAvatar("/old.png");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        // Act
        companyController.update(form, bindingResult);

        // Assert
        verify(fileService).deleteFile("/old.png");
    }

    @Test
    void shouldNotDeleteOldAvatarWhenUpdateAvatarUnchanged() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateCompanyForm form = new UpdateCompanyForm();
        form.setId(1L);
        form.setName("Company One");
        form.setAvatar("/same.png");

        Company company = new Company();
        company.setId(1L);
        company.setName("Company One");
        company.setAvatar("/same.png");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        // Act
        companyController.update(form, bindingResult);

        // Assert
        verify(fileService, never()).deleteFile(any());
        verify(companyRepository, never()).existsByName(any());
    }

    // --------------------------------------------------------------------- get

    @Test
    void shouldThrowNotFoundWhenGetIdMissing() {
        // Arrange
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> companyController.get(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldGetCompanySuccessfully() {
        // Arrange
        Company company = new Company();
        company.setId(1L);
        company.setName("Company One");
        company.setAvatar("/avatar/one.png");

        CompanyDto dto = new CompanyDto();
        dto.setId(1L);
        dto.setName("Company One");
        dto.setAvatar("/avatar/one.png");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyMapper.fromEntityToCompanyDto(company)).thenReturn(dto);

        // Act
        ApiMessageDto<CompanyDto> result = companyController.get(1L);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData()).isEqualTo(dto);
    }

    // ------------------------------------------------------------------ delete

    @Test
    void shouldThrowNotFoundWhenDeleteIdMissing() {
        // Arrange
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> companyController.delete(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeleteAvatarFileWhenDeletingCompanyWithNonBlankAvatar() {
        // Arrange
        Company company = new Company();
        company.setId(1L);
        company.setAvatar("/to-delete.png");
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        // Act
        ApiMessageDto<Void> result = companyController.delete(1L);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(fileService).deleteFile("/to-delete.png");
        verify(companyRepository).deleteById(1L);
    }

    @Test
    void shouldCallDeleteFileEvenWhenAvatarIsBlank() {
        // Arrange: FileService.deleteFile is null-safe internally (isDeletable returns false for
        // a blank/null path and no-ops), so the controller no longer duplicates that check.
        Company company = new Company();
        company.setId(1L);
        company.setAvatar(null);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        // Act
        ApiMessageDto<Void> result = companyController.delete(1L);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(fileService).deleteFile(null);
        verify(companyRepository).deleteById(1L);
    }

    @Test
    void shouldCascadeDeleteJobPostingsBeforeDeletingCompany() {
        // Arrange (FR-010): deleting a Company must cascade-delete its JobPostings first to
        // avoid orphan rows / FK constraint errors.
        Company company = new Company();
        company.setId(1L);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        // Act
        ApiMessageDto<Void> result = companyController.delete(1L);

        // Assert
        assertThat(result.getResult()).isTrue();
        InOrder inOrder = inOrder(jobPostingRepository, companyRepository);
        inOrder.verify(jobPostingRepository).deleteAllByCompanyId(1L);
        inOrder.verify(companyRepository).deleteById(1L);
    }
}
