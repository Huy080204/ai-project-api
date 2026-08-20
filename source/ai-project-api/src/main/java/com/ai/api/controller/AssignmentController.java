package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.assignment.AssignmentDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.assignment.CreateAssignmentForm;
import com.ai.api.form.assignment.UpdateAssignmentForm;
import com.ai.api.mapper.AssignmentMapper;
import com.ai.api.model.Assignment;
import com.ai.api.model.Syllabus;
import com.ai.api.model.criteria.AssignmentCriteria;
import com.ai.api.repository.AssignmentRepository;
import com.ai.api.repository.SubmissionRepository;
import com.ai.api.repository.SyllabusRepository;
import com.ai.api.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1/assignment")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class AssignmentController extends ABasicController {
    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SyllabusRepository syllabusRepository;

    @Autowired
    private AssignmentMapper assignmentMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private SubmissionRepository submissionRepository;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ASM_C')")
    @Transactional
    public ApiMessageDto<AssignmentDto> create(@Valid @RequestBody CreateAssignmentForm createAssignmentForm) {
        Syllabus syllabus = syllabusRepository.findById(createAssignmentForm.getSyllabusId())
                .orElseThrow(() -> new NotFoundException("Syllabus not found", ErrorCode.SYLLABUS_ERROR_NOT_FOUND));

        Assignment assignment = assignmentMapper.fromFormToEntity(createAssignmentForm);
        assignment.setSyllabus(syllabus);
        assignmentRepository.save(assignment);
        return makeSuccessResponse(assignmentMapper.fromEntityToAssignmentIdDto(assignment), "Create assignment success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ASM_U')")
    @Transactional
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateAssignmentForm updateAssignmentForm) {
        Assignment assignment = assignmentRepository.findById(updateAssignmentForm.getId())
                .orElseThrow(() -> new NotFoundException("Assignment not found", ErrorCode.ASSIGNMENT_ERROR_NOT_FOUND));

        if (AIConstant.ASSIGNMENT_STATE_CLOSED.equals(assignment.getState())) {
            throw new BadRequestException("Assignment is closed and cannot be updated", ErrorCode.ASSIGNMENT_ERROR_UNABLE_UPDATE);
        }

        String oldFileAttachmentUrl = assignment.getFileAttachmentUrl();
        if (oldFileAttachmentUrl != null && updateAssignmentForm.getFileAttachmentUrl() != null
                && !oldFileAttachmentUrl.equals(updateAssignmentForm.getFileAttachmentUrl())) {
            fileService.deleteFile(oldFileAttachmentUrl);
        }

        assignmentMapper.updateEntityFromForm(updateAssignmentForm, assignment);
        assignmentRepository.save(assignment);
        return makeSuccessResponse("Update assignment success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ASM_V')")
    public ApiMessageDto<AssignmentDto> get(@PathVariable Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assignment not found", ErrorCode.ASSIGNMENT_ERROR_NOT_FOUND));
        return makeSuccessResponse(assignmentMapper.fromEntityToAssignmentDto(assignment), "Get assignment success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ASM_L')")
    public ApiMessageDto<ResponseListDto<List<AssignmentDto>>> list(AssignmentCriteria assignmentCriteria, Pageable pageable) {
        Page<Assignment> page = assignmentRepository.findAll(assignmentCriteria.getCriteria(), pageable);
        ResponseListDto<List<AssignmentDto>> responseListDto =
                makeResponseListDto(page, assignmentMapper::fromEntityToAssignmentDtoList);
        return makeSuccessResponse(responseListDto, "Get list success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ASM_D')")
    @Transactional
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assignment not found", ErrorCode.ASSIGNMENT_ERROR_NOT_FOUND));

        List<String> filesToDelete = new ArrayList<>();
        if (assignment.getFileAttachmentUrl() != null) {
            filesToDelete.add(assignment.getFileAttachmentUrl());
        }
        filesToDelete.addAll(submissionRepository.findFileUrlsByAssignmentId(id));
        if (!filesToDelete.isEmpty()) {
            fileService.deleteFiles(filesToDelete);
        }

        submissionRepository.deleteAllByAssignmentId(id);

        assignmentRepository.deleteById(id);
        return makeSuccessResponse("Delete assignment success");
    }
}
