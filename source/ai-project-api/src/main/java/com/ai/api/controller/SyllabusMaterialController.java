package com.ai.api.controller;

import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.syllabusmaterial.SyllabusMaterialDto;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.syllabusmaterial.CreateSyllabusMaterialForm;
import com.ai.api.form.syllabusmaterial.UpdateSyllabusMaterialForm;
import com.ai.api.mapper.SyllabusMaterialMapper;
import com.ai.api.model.Syllabus;
import com.ai.api.model.SyllabusMaterial;
import com.ai.api.model.criteria.SyllabusMaterialCriteria;
import com.ai.api.repository.SyllabusMaterialRepository;
import com.ai.api.repository.SyllabusRepository;
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
import java.util.Objects;

@RestController
@RequestMapping("/v1/syllabus-material")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class SyllabusMaterialController extends ABasicController {
    @Autowired
    private SyllabusMaterialRepository syllabusMaterialRepository;

    @Autowired
    private SyllabusRepository syllabusRepository;

    @Autowired
    private SyllabusMaterialMapper syllabusMaterialMapper;

    @Autowired
    private FileService fileService;

    @Transactional
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SM_C')")
    public ApiMessageDto<SyllabusMaterialDto> create(@Valid @RequestBody CreateSyllabusMaterialForm createForm, BindingResult bindingResult) {
        Syllabus syllabus = syllabusRepository.findById(createForm.getSyllabusId())
                .orElseThrow(() -> new NotFoundException("Syllabus not found", ErrorCode.SYLLABUS_ERROR_NOT_FOUND));

        SyllabusMaterial syllabusMaterial = syllabusMaterialMapper.fromCreateSyllabusMaterialFormToEntity(createForm);
        syllabusMaterial.setSyllabus(syllabus);
        syllabusMaterialRepository.save(syllabusMaterial);
        return makeSuccessResponse(syllabusMaterialMapper.fromEntityToSyllabusMaterialIdDto(syllabusMaterial), "Create syllabus material success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SM_V')")
    public ApiMessageDto<SyllabusMaterialDto> get(@PathVariable("id") Long id) {
        SyllabusMaterial syllabusMaterial = syllabusMaterialRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Syllabus material not found", ErrorCode.SYLLABUS_MATERIAL_ERROR_NOT_FOUND));
        return makeSuccessResponse(syllabusMaterialMapper.fromEntityToSyllabusMaterialDto(syllabusMaterial), "Get syllabus material success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SM_L')")
    public ApiMessageDto<ResponseListDto<List<SyllabusMaterialDto>>> list(SyllabusMaterialCriteria criteria, @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SyllabusMaterial> page = syllabusMaterialRepository.findAll(criteria.getCriteria(), pageable);
        ResponseListDto<List<SyllabusMaterialDto>> responseListDto =
                makeResponseListDto(page, syllabusMaterialMapper::fromEntityToSyllabusMaterialDtoList);
        return makeSuccessResponse(responseListDto, "Get list syllabus material success");
    }

    @Transactional
    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SM_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateSyllabusMaterialForm updateForm, BindingResult bindingResult) {
        SyllabusMaterial syllabusMaterial = syllabusMaterialRepository.findById(updateForm.getId())
                .orElseThrow(() -> new NotFoundException("Syllabus material not found", ErrorCode.SYLLABUS_MATERIAL_ERROR_NOT_FOUND));

        if (StringUtils.isNoneBlank(syllabusMaterial.getFileUrl())
                && !Objects.equals(updateForm.getFileUrl(), syllabusMaterial.getFileUrl())) {
            fileService.deleteFile(syllabusMaterial.getFileUrl());
        }
        syllabusMaterialMapper.updateEntityFromForm(updateForm, syllabusMaterial);
        syllabusMaterialRepository.save(syllabusMaterial);
        return makeSuccessResponse("Update syllabus material success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SM_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        SyllabusMaterial syllabusMaterial = syllabusMaterialRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Syllabus material not found", ErrorCode.SYLLABUS_MATERIAL_ERROR_NOT_FOUND));

        if (StringUtils.isNoneBlank(syllabusMaterial.getFileUrl())) {
            fileService.deleteFile(syllabusMaterial.getFileUrl());
        }
        syllabusMaterialRepository.deleteById(id);
        return makeSuccessResponse("Delete syllabus material success");
    }
}
