package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/job-posting")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class JobPostingController extends ABasicController {
    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobPostingMapper jobPostingMapper;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('JP_C')")
    @Transactional
    public ApiMessageDto<JobPostingDto> create(@Valid @RequestBody CreateJobPostingForm form, BindingResult bindingResult) {
        Company company = companyRepository.findById(form.getCompanyId())
                .orElseThrow(() -> new NotFoundException("[JobPosting] Company not found", ErrorCode.JOB_POSTING_ERROR_COMPANY_NOT_FOUND));

        JobPosting jobPosting = jobPostingMapper.fromFormToEntity(form);
        jobPosting.setCompany(company);
        jobPosting.setState(AIConstant.JOB_POSTING_STATE_OPEN);
        jobPostingRepository.save(jobPosting);
        return makeSuccessResponse(jobPostingMapper.fromEntityToJobPostingIdDto(jobPosting), "Create JobPosting success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('JP_U')")
    @Transactional
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateJobPostingForm form, BindingResult bindingResult) {
        JobPosting jobPosting = jobPostingRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[JobPosting] JobPosting not found", ErrorCode.JOB_POSTING_ERROR_NOT_FOUND));
        Company company = companyRepository.findById(form.getCompanyId())
                .orElseThrow(() -> new NotFoundException("[JobPosting] Company not found", ErrorCode.JOB_POSTING_ERROR_COMPANY_NOT_FOUND));

        jobPostingMapper.updateEntityFromForm(form, jobPosting);
        jobPosting.setCompany(company);
        jobPostingRepository.save(jobPosting);
        return makeSuccessResponse("Update JobPosting success");
    }

    @PutMapping(value = "/change-state", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('JP_U')")
    @Transactional
    public ApiMessageDto<Void> changeState(@Valid @RequestBody UpdateJobPostingStateForm form, BindingResult bindingResult) {
        JobPosting jobPosting = jobPostingRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[JobPosting] JobPosting not found", ErrorCode.JOB_POSTING_ERROR_NOT_FOUND));

        jobPosting.setState(form.getState());
        jobPostingRepository.save(jobPosting);
        return makeSuccessResponse("Update JobPosting state success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('JP_V')")
    public ApiMessageDto<JobPostingDto> get(@PathVariable Long id) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[JobPosting] JobPosting not found", ErrorCode.JOB_POSTING_ERROR_NOT_FOUND));
        return makeSuccessResponse(jobPostingMapper.fromEntityToJobPostingDto(jobPosting), "Get JobPosting success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('JP_L')")
    public ApiMessageDto<ResponseListDto<List<JobPostingDto>>> list(JobPostingCriteria criteria, @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<JobPosting> page = jobPostingRepository.findAll(criteria.getCriteria(), pageable);
        return makeSuccessResponse(makeResponseListDto(page, jobPostingMapper::fromEntityToJobPostingDtoList), "List JobPosting success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('JP_D')")
    @Transactional
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        jobPostingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[JobPosting] JobPosting not found", ErrorCode.JOB_POSTING_ERROR_NOT_FOUND));
        jobPostingRepository.deleteById(id);
        return makeSuccessResponse("Delete JobPosting success");
    }
}
