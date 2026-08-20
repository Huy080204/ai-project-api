package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.company.CompanyDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.company.CreateCompanyForm;
import com.ai.api.form.company.UpdateCompanyForm;
import com.ai.api.mapper.CompanyMapper;
import com.ai.api.model.Company;
import com.ai.api.model.criteria.CompanyCriteria;
import com.ai.api.repository.CompanyRepository;
import com.ai.api.repository.JobPostingRepository;
import com.ai.api.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/v1/company")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class CompanyController extends ABasicController {
    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COM_C')")
    @Transactional
    public ApiMessageDto<CompanyDto> create(@Valid @RequestBody CreateCompanyForm form, BindingResult bindingResult) {
        if (companyRepository.existsByName(form.getName())) {
            throw new BadRequestException("[Company] Company name exist", ErrorCode.COMPANY_ERROR_NAME_EXIST);
        }
        Company company = companyMapper.fromFormToEntity(form);
        companyRepository.save(company);
        return makeSuccessResponse(companyMapper.fromEntityToCompanyIdDto(company), "Create Company success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COM_U')")
    @Transactional
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateCompanyForm form, BindingResult bindingResult) {
        Company company = companyRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Company] Company not found", ErrorCode.COMPANY_ERROR_NOT_FOUND));
        if (!Objects.equals(form.getName(), company.getName()) && companyRepository.existsByName(form.getName())) {
            throw new BadRequestException("[Company] Company name exist", ErrorCode.COMPANY_ERROR_NAME_EXIST);
        }
        String oldAvatar = company.getAvatar();
        if (StringUtils.isNoneBlank(oldAvatar) && !Objects.equals(form.getAvatar(), oldAvatar)) {
            fileService.deleteFile(oldAvatar);
        }
        companyMapper.updateEntityFromForm(form, company);
        companyRepository.save(company);
        return makeSuccessResponse("Update Company success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COM_V')")
    public ApiMessageDto<CompanyDto> get(@PathVariable Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Company] Company not found", ErrorCode.COMPANY_ERROR_NOT_FOUND));
        return makeSuccessResponse(companyMapper.fromEntityToCompanyDto(company), "Get Company success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COM_L')")
    public ApiMessageDto<ResponseListDto<List<CompanyDto>>> list(CompanyCriteria criteria, @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Company> companies = companyRepository.findAll(criteria.getCriteria(), pageable);
        return makeSuccessResponse(makeResponseListDto(companies, companyMapper::fromEntityToCompanyDtoList), "List Company success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COM_D')")
    @Transactional
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Company] Company not found", ErrorCode.COMPANY_ERROR_NOT_FOUND));
        fileService.deleteFile(company.getAvatar());
        jobPostingRepository.deleteAllByCompanyId(id);
        companyRepository.deleteById(id);
        return makeSuccessResponse("Delete Company success");
    }

    @GetMapping(value = "/public/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<CompanyDto>>> publicList(CompanyCriteria criteria, @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        criteria.setStatus(AIConstant.STATUS_ACTIVE);
        Page<Company> companies = companyRepository.findAll(criteria.getCriteria(), pageable);
        return makeSuccessResponse(makeResponseListDto(companies, companyMapper::fromEntityToCompanyDtoPublicList), "List Company success");
    }
}
