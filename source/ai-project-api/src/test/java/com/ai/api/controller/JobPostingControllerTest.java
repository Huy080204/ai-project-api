package com.ai.api.controller;

import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.jobposting.JobPostingDto;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.jobposting.CreateJobPostingForm;
import com.ai.api.form.jobposting.UpdateJobPostingForm;
import com.ai.api.form.jobposting.UpdateJobPostingStateForm;
import com.ai.api.mapper.JobPostingMapper;
import com.ai.api.model.Company;
import com.ai.api.model.JobPosting;
import com.ai.api.model.criteria.JobPostingCriteria;
import com.ai.api.repository.CompanyRepository;
import com.ai.api.repository.JobPostingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.validation.BindingResult;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link JobPostingController}. JobPostingController does not exist yet — this
 * test is written against its intended API and is expected to fail to compile until it lands,
 * same red->green precedent as CompanyControllerTest.
 */
@ExtendWith(MockitoExtension.class)
class JobPostingControllerTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    private JobPostingRepository jobPostingRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private JobPostingMapper jobPostingMapper;

    @InjectMocks
    private JobPostingController jobPostingController;

    // ------------------------------------------------------------------ create

    @Test
    void create_whenSuccess_returnsSuccessResponse() {
        BindingResult bindingResult = mock(BindingResult.class);
        CreateJobPostingForm form = new CreateJobPostingForm();
        form.setPosition("Backend Developer");
        form.setDescription("Build APIs");
        form.setSalary(new BigDecimal("1500.00"));
        form.setCompanyId(1L);

        Company company = new Company();
        company.setId(1L);
        JobPosting jobPosting = new JobPosting();

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(jobPostingMapper.fromFormToEntity(form)).thenReturn(jobPosting);

        ApiMessageDto<JobPostingDto> result = jobPostingController.create(form, bindingResult);

        assertThat(result.getResult()).isTrue();
        verify(jobPostingRepository).save(jobPosting);
    }

    @Test
    void create_whenCompanyNotFound_throwsNotFoundException() {
        BindingResult bindingResult = mock(BindingResult.class);
        CreateJobPostingForm form = new CreateJobPostingForm();
        form.setPosition("Backend Developer");
        form.setDescription("Build APIs");
        form.setSalary(new BigDecimal("1500.00"));
        form.setCompanyId(99L);

        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobPostingController.create(form, bindingResult))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.JOB_POSTING_ERROR_COMPANY_NOT_FOUND);
    }

    @Test
    void create_whenSuccess_savedRowAlwaysStartsAtStateZero() {
        // CreateJobPostingForm has no `state` field at all (Principle IV/Auditable-defaults
        // style guard), so a freshly mapped entity always carries the entity's own default
        // regardless of any `state` a raw payload might smuggle in.
        BindingResult bindingResult = mock(BindingResult.class);
        CreateJobPostingForm form = new CreateJobPostingForm();
        form.setPosition("Backend Developer");
        form.setDescription("Build APIs");
        form.setSalary(new BigDecimal("1500.00"));
        form.setCompanyId(1L);

        Company company = new Company();
        company.setId(1L);
        JobPosting jobPosting = new JobPosting();

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(jobPostingMapper.fromFormToEntity(form)).thenReturn(jobPosting);

        jobPostingController.create(form, bindingResult);

        assertThat(jobPosting.getState()).isEqualTo(0);
    }

    @Test
    void createForm_whenPositionBlank_failsBeanValidation() {
        CreateJobPostingForm form = new CreateJobPostingForm();
        form.setPosition(" ");
        form.setDescription("Build APIs");
        form.setSalary(new BigDecimal("1500.00"));
        form.setCompanyId(1L);

        Set<ConstraintViolation<CreateJobPostingForm>> violations = VALIDATOR.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("position"));
    }

    @Test
    void createForm_whenDescriptionBlank_failsBeanValidation() {
        CreateJobPostingForm form = new CreateJobPostingForm();
        form.setPosition("Backend Developer");
        form.setDescription(" ");
        form.setSalary(new BigDecimal("1500.00"));
        form.setCompanyId(1L);

        Set<ConstraintViolation<CreateJobPostingForm>> violations = VALIDATOR.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("description"));
    }

    @Test
    void createForm_whenSalaryNull_failsBeanValidation() {
        CreateJobPostingForm form = new CreateJobPostingForm();
        form.setPosition("Backend Developer");
        form.setDescription("Build APIs");
        form.setSalary(null);
        form.setCompanyId(1L);

        Set<ConstraintViolation<CreateJobPostingForm>> violations = VALIDATOR.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("salary"));
    }

    // ------------------------------------------------------------------ update

    @Test
    void update_whenSuccess_updatesFieldsWithStateUnchanged() {
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateJobPostingForm form = new UpdateJobPostingForm();
        form.setId(1L);
        form.setPosition("Senior Backend Developer");
        form.setDescription("Updated description");
        form.setSalary(new BigDecimal("2000.00"));
        form.setCompanyId(2L);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setId(1L);
        jobPosting.setState(1);

        Company company = new Company();
        company.setId(2L);

        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(jobPosting));
        when(companyRepository.findById(2L)).thenReturn(Optional.of(company));

        ApiMessageDto<Void> result = jobPostingController.update(form, bindingResult);

        assertThat(result.getResult()).isTrue();
        assertThat(jobPosting.getState()).isEqualTo(1);
        verify(jobPostingMapper).updateEntityFromForm(form, jobPosting);
        verify(jobPostingRepository).save(jobPosting);
    }

    @Test
    void update_whenNotFound_throwsNotFoundException() {
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateJobPostingForm form = new UpdateJobPostingForm();
        form.setId(1L);
        form.setPosition("Backend Developer");
        form.setDescription("Build APIs");
        form.setSalary(new BigDecimal("1500.00"));
        form.setCompanyId(1L);

        when(jobPostingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobPostingController.update(form, bindingResult))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.JOB_POSTING_ERROR_NOT_FOUND);
    }

    @Test
    void update_whenCompanyNotFound_throwsNotFoundException() {
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateJobPostingForm form = new UpdateJobPostingForm();
        form.setId(1L);
        form.setPosition("Backend Developer");
        form.setDescription("Build APIs");
        form.setSalary(new BigDecimal("1500.00"));
        form.setCompanyId(99L);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setId(1L);

        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(jobPosting));
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobPostingController.update(form, bindingResult))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.JOB_POSTING_ERROR_COMPANY_NOT_FOUND);
    }

    // ------------------------------------------------------------- change-state

    @Test
    void changeState_whenSuccess_setsStateToOneWithoutChangingOtherFields() {
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateJobPostingStateForm form = new UpdateJobPostingStateForm();
        form.setId(1L);
        form.setState(1);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setId(1L);
        jobPosting.setPosition("Backend Developer");
        jobPosting.setState(0);

        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(jobPosting));

        ApiMessageDto<Void> result = jobPostingController.changeState(form, bindingResult);

        assertThat(result.getResult()).isTrue();
        assertThat(jobPosting.getState()).isEqualTo(1);
        assertThat(jobPosting.getPosition()).isEqualTo("Backend Developer");
        verify(jobPostingRepository).save(jobPosting);
    }

    @Test
    void changeState_whenSuccess_setsStateToZeroWithoutChangingOtherFields() {
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateJobPostingStateForm form = new UpdateJobPostingStateForm();
        form.setId(1L);
        form.setState(0);

        JobPosting jobPosting = new JobPosting();
        jobPosting.setId(1L);
        jobPosting.setPosition("Backend Developer");
        jobPosting.setState(1);

        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(jobPosting));

        ApiMessageDto<Void> result = jobPostingController.changeState(form, bindingResult);

        assertThat(result.getResult()).isTrue();
        assertThat(jobPosting.getState()).isEqualTo(0);
        assertThat(jobPosting.getPosition()).isEqualTo("Backend Developer");
        verify(jobPostingRepository).save(jobPosting);
    }

    @Test
    void changeState_whenNotFound_throwsNotFoundException() {
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateJobPostingStateForm form = new UpdateJobPostingStateForm();
        form.setId(1L);
        form.setState(1);

        when(jobPostingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobPostingController.changeState(form, bindingResult))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.JOB_POSTING_ERROR_NOT_FOUND);
    }

    @Test
    void updateStateForm_whenStateIsTwo_failsBeanValidation() {
        UpdateJobPostingStateForm form = new UpdateJobPostingStateForm();
        form.setId(1L);
        form.setState(2);

        Set<ConstraintViolation<UpdateJobPostingStateForm>> violations = VALIDATOR.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("state"));
    }

    @Test
    void updateStateForm_whenStateMissing_failsBeanValidation() {
        UpdateJobPostingStateForm form = new UpdateJobPostingStateForm();
        form.setId(1L);
        form.setState(null);

        Set<ConstraintViolation<UpdateJobPostingStateForm>> violations = VALIDATOR.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("state"));
    }

    // --------------------------------------------------------------------- get

    @Test
    void get_whenFound_returnsSuccessDto() {
        JobPosting jobPosting = new JobPosting();
        jobPosting.setId(1L);

        JobPostingDto dto = new JobPostingDto();
        dto.setId(1L);

        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(jobPosting));
        when(jobPostingMapper.fromEntityToJobPostingDto(jobPosting)).thenReturn(dto);

        ApiMessageDto<JobPostingDto> result = jobPostingController.get(1L);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getData()).isEqualTo(dto);
    }

    @Test
    void get_whenNotFound_throwsNotFoundException() {
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobPostingController.get(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.JOB_POSTING_ERROR_NOT_FOUND);
    }

    // -------------------------------------------------------------------- list

    @Test
    void list_delegatesToRepositoryFindAllWithCriteriaAndPageable() {
        JobPostingCriteria criteria = new JobPostingCriteria();
        criteria.setId(1L);
        criteria.setPosition("Developer");
        criteria.setCompanyId(2L);
        criteria.setState(1);
        Pageable pageable = PageRequest.of(0, 10);

        JobPosting jobPosting = new JobPosting();
        JobPostingDto dto = new JobPostingDto();
        Page<JobPosting> page = new PageImpl<>(Collections.singletonList(jobPosting));

        when(jobPostingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(jobPostingMapper.fromEntityToJobPostingDtoList(page.getContent()))
                .thenReturn(Collections.singletonList(dto));

        ApiMessageDto<ResponseListDto<List<JobPostingDto>>> result = jobPostingController.list(criteria, pageable);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).containsExactly(dto);
        verify(jobPostingRepository).findAll(any(Specification.class), eq(pageable));
    }

    // ------------------------------------------------------------------ delete

    @Test
    void delete_whenFound_deletesSuccessfully() {
        JobPosting jobPosting = new JobPosting();
        jobPosting.setId(1L);
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(jobPosting));

        ApiMessageDto<Void> result = jobPostingController.delete(1L);

        assertThat(result.getResult()).isTrue();
        verify(jobPostingRepository).deleteById(1L);
    }

    @Test
    void delete_whenNotFound_throwsNotFoundException() {
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobPostingController.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.JOB_POSTING_ERROR_NOT_FOUND);
    }
}
