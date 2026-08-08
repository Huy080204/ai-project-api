package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.submission.SubmissionDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.submission.CreateSubmissionForm;
import com.ai.api.form.submission.GradeSubmissionForm;
import com.ai.api.form.submission.UpdateSubmissionForm;
import com.ai.api.mapper.SubmissionMapper;
import com.ai.api.model.Assignment;
import com.ai.api.model.Student;
import com.ai.api.model.Submission;
import com.ai.api.model.criteria.SubmissionCriteria;
import com.ai.api.repository.AssignmentRepository;
import com.ai.api.repository.StudentRepository;
import com.ai.api.repository.SubmissionRepository;
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
import java.util.List;

@RestController
@RequestMapping("/v1/submission")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class SubmissionController extends ABasicController {
    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private FileService fileService;

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SUB_V')")
    public ApiMessageDto<SubmissionDto> get(@PathVariable Long id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found submission!", ErrorCode.SUBMISSION_ERROR_NOT_FOUND));
        return makeSuccessResponse(submissionMapper.fromEntityToSubmissionDto(submission), "Get submission success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SUB_L')")
    public ApiMessageDto<ResponseListDto<List<SubmissionDto>>> list(SubmissionCriteria criteria, Pageable pageable) {
        Page<Submission> page = submissionRepository.findAll(criteria.getCriteria(), pageable);
        ResponseListDto<List<SubmissionDto>> responseListDto =
                makeResponseListDto(page, submissionMapper::fromEntityToSubmissionDtoList);
        return makeSuccessResponse(responseListDto, "Get list success");
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SUB_C')")
    @Transactional
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateSubmissionForm createSubmissionForm) {
        Assignment assignment = assignmentRepository.findById(createSubmissionForm.getAssignmentId())
                .orElseThrow(() -> new NotFoundException("Not found assignment!", ErrorCode.ASSIGNMENT_ERROR_NOT_FOUND));
        Student student = studentRepository.findById(createSubmissionForm.getStudentId())
                .orElseThrow(() -> new NotFoundException("Not found student!", ErrorCode.STUDENT_ERROR_NOT_FOUND));

        if (AIConstant.ASSIGNMENT_STATE_CLOSED.equals(assignment.getState())) {
            throw new BadRequestException("Assignment is closed", ErrorCode.SUBMISSION_ERROR_ASSIGNMENT_CLOSED);
        }

        Submission submission = submissionMapper.fromFormToEntity(createSubmissionForm);
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submissionRepository.save(submission);
        return makeSuccessResponse("Create submission success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SUB_U')")
    @Transactional
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateSubmissionForm updateSubmissionForm) {
        Submission submission = submissionRepository.findById(updateSubmissionForm.getId())
                .orElseThrow(() -> new NotFoundException("Not found submission!", ErrorCode.SUBMISSION_ERROR_NOT_FOUND));

        if (AIConstant.SUBMISSION_STATE_GRADED.equals(submission.getState())) {
            throw new BadRequestException("Submission already graded", ErrorCode.SUBMISSION_ERROR_ALREADY_GRADED);
        }

        Assignment assignment = assignmentRepository.findById(updateSubmissionForm.getAssignmentId())
                .orElseThrow(() -> new NotFoundException("Assignment not found", ErrorCode.ASSIGNMENT_ERROR_NOT_FOUND));
        if (AIConstant.ASSIGNMENT_STATE_CLOSED.equals(assignment.getState())) {
            throw new BadRequestException("Assignment is closed", ErrorCode.SUBMISSION_ERROR_ASSIGNMENT_CLOSED);
        }
        Student student = studentRepository.findById(updateSubmissionForm.getStudentId())
                .orElseThrow(() -> new NotFoundException("Student not found", ErrorCode.STUDENT_ERROR_NOT_FOUND));

        String oldFileUrl = submission.getFileUrl();
        if (oldFileUrl != null && updateSubmissionForm.getFileUrl() != null
                && !oldFileUrl.equals(updateSubmissionForm.getFileUrl())) {
            fileService.deleteFile(oldFileUrl);
        }

        submissionMapper.updateEntityFromForm(updateSubmissionForm, submission);
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submissionRepository.save(submission);
        return makeSuccessResponse("Update submission success");
    }

    @PutMapping(value = "/grade", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SUB_U_G')")
    @Transactional
    public ApiMessageDto<Void> grade(@Valid @RequestBody GradeSubmissionForm gradeSubmissionForm) {
        Submission submission = submissionRepository.findById(gradeSubmissionForm.getId())
                .orElseThrow(() -> new NotFoundException("Not found submission!", ErrorCode.SUBMISSION_ERROR_NOT_FOUND));

        if (AIConstant.SUBMISSION_STATE_GRADED.equals(submission.getState())) {
            throw new BadRequestException("Submission already graded", ErrorCode.SUBMISSION_ERROR_ALREADY_GRADED);
        }

        submissionMapper.updateEntityFromGradeForm(gradeSubmissionForm, submission);
        submission.setState(AIConstant.SUBMISSION_STATE_GRADED);
        submissionRepository.save(submission);
        return makeSuccessResponse("Grade submission success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SUB_D')")
    @Transactional
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found submission!", ErrorCode.SUBMISSION_ERROR_NOT_FOUND));

        if (submission.getFileUrl() != null) {
            fileService.deleteFile(submission.getFileUrl());
        }

        submissionRepository.deleteById(id);
        return makeSuccessResponse("Delete submission success");
    }
}
