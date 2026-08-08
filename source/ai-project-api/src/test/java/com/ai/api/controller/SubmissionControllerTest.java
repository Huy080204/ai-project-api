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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionControllerTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SubmissionMapper submissionMapper;

    @Mock
    private FileService fileService;

    @InjectMocks
    private SubmissionController submissionController;

    @Test
    void create_whenSuccess_savesSubmissionWithPendingState() {
        CreateSubmissionForm form = new CreateSubmissionForm();
        form.setAssignmentId(1L);
        form.setStudentId(2L);
        form.setContent("my work");

        Assignment assignment = new Assignment();
        assignment.setState(AIConstant.ASSIGNMENT_STATE_PUBLISHED);
        Student student = new Student();
        Submission submission = new Submission();

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(submissionMapper.fromFormToEntity(form)).thenReturn(submission);

        ApiMessageDto<Void> result = submissionController.create(form);

        assertThat(result.getResult()).isTrue();
        assertThat(submission.getState()).isEqualTo(0);
        assertThat(submission.getAssignment()).isSameAs(assignment);
        assertThat(submission.getStudent()).isSameAs(student);
        verify(submissionRepository).save(submission);
    }

    @Test
    void create_whenAssignmentNotFound_throwsNotFoundException() {
        CreateSubmissionForm form = new CreateSubmissionForm();
        form.setAssignmentId(99L);
        form.setStudentId(2L);

        when(assignmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissionController.create(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ASSIGNMENT_ERROR_NOT_FOUND);

        verify(submissionRepository, never()).save(any());
    }

    @Test
    void create_whenStudentNotFound_throwsNotFoundException() {
        CreateSubmissionForm form = new CreateSubmissionForm();
        form.setAssignmentId(1L);
        form.setStudentId(99L);

        Assignment assignment = new Assignment();
        assignment.setState(AIConstant.ASSIGNMENT_STATE_PUBLISHED);

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissionController.create(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.STUDENT_ERROR_NOT_FOUND);

        verify(submissionRepository, never()).save(any());
    }

    @Test
    void create_whenAssignmentClosed_throwsBadRequestExceptionAndPersistsNothing() {
        CreateSubmissionForm form = new CreateSubmissionForm();
        form.setAssignmentId(1L);
        form.setStudentId(2L);

        Assignment assignment = new Assignment();
        assignment.setState(AIConstant.ASSIGNMENT_STATE_CLOSED);
        Student student = new Student();

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> submissionController.create(form))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.SUBMISSION_ERROR_ASSIGNMENT_CLOSED);

        verify(submissionRepository, never()).save(any());
    }

    @Test
    void update_whenSuccess_updatesSubmissionAssignmentAndStudent() {
        UpdateSubmissionForm form = new UpdateSubmissionForm();
        form.setId(10L);
        form.setAssignmentId(1L);
        form.setStudentId(2L);
        form.setContent("updated content");

        Assignment openAssignment = new Assignment();
        openAssignment.setState(AIConstant.ASSIGNMENT_STATE_PUBLISHED);
        Student student = new Student();

        Submission existing = new Submission();

        when(submissionRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(openAssignment));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));

        ApiMessageDto<Void> result = submissionController.update(form);

        assertThat(result.getResult()).isTrue();
        assertThat(existing.getAssignment()).isSameAs(openAssignment);
        assertThat(existing.getStudent()).isSameAs(student);
        verify(submissionMapper).updateEntityFromForm(form, existing);
        verify(submissionRepository).save(existing);
    }

    @Test
    void update_whenNewAssignmentNotFound_throwsNotFoundException() {
        UpdateSubmissionForm form = new UpdateSubmissionForm();
        form.setId(10L);
        form.setAssignmentId(99L);
        form.setStudentId(2L);

        Submission existing = new Submission();

        when(submissionRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(assignmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissionController.update(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ASSIGNMENT_ERROR_NOT_FOUND);

        verify(submissionMapper, never()).updateEntityFromForm(any(), any());
        verify(submissionRepository, never()).save(any());
    }

    @Test
    void update_whenNewStudentNotFound_throwsNotFoundException() {
        UpdateSubmissionForm form = new UpdateSubmissionForm();
        form.setId(10L);
        form.setAssignmentId(1L);
        form.setStudentId(99L);

        Assignment openAssignment = new Assignment();
        openAssignment.setState(AIConstant.ASSIGNMENT_STATE_PUBLISHED);

        Submission existing = new Submission();

        when(submissionRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(openAssignment));
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> submissionController.update(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.STUDENT_ERROR_NOT_FOUND);

        verify(submissionMapper, never()).updateEntityFromForm(any(), any());
        verify(submissionRepository, never()).save(any());
    }

    @Test
    void update_whenAlreadyGraded_throwsBadRequestExceptionAndChangesNoField() {
        UpdateSubmissionForm form = new UpdateSubmissionForm();
        form.setId(10L);
        form.setContent("updated content");

        Submission existing = new Submission();
        existing.setState(1);
        existing.setContent("original content");

        when(submissionRepository.findById(10L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> submissionController.update(form))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.SUBMISSION_ERROR_ALREADY_GRADED);

        assertThat(existing.getContent()).isEqualTo("original content");
        verify(submissionMapper, never()).updateEntityFromForm(any(), any());
        verify(submissionRepository, never()).save(any());
    }

    @Test
    void update_whenNewAssignmentClosed_throwsBadRequestExceptionEvenIfOriginalAssignmentWasOpen() {
        UpdateSubmissionForm form = new UpdateSubmissionForm();
        form.setId(10L);
        form.setAssignmentId(1L);
        form.setStudentId(2L);
        form.setContent("updated content");

        Assignment originalOpenAssignment = new Assignment();
        originalOpenAssignment.setState(AIConstant.ASSIGNMENT_STATE_PUBLISHED);

        Assignment newClosedAssignment = new Assignment();
        newClosedAssignment.setState(AIConstant.ASSIGNMENT_STATE_CLOSED);

        Submission existing = new Submission();
        existing.setAssignment(originalOpenAssignment);
        existing.setContent("original content");

        when(submissionRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(newClosedAssignment));

        assertThatThrownBy(() -> submissionController.update(form))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.SUBMISSION_ERROR_ASSIGNMENT_CLOSED);

        assertThat(existing.getContent()).isEqualTo("original content");
        verify(submissionMapper, never()).updateEntityFromForm(any(), any());
        verify(submissionRepository, never()).save(any());
    }

    @Test
    void update_whenFileUrlChanged_deletesOldFile() {
        UpdateSubmissionForm form = new UpdateSubmissionForm();
        form.setId(10L);
        form.setAssignmentId(1L);
        form.setStudentId(2L);
        form.setFileUrl("new-file.pdf");

        Assignment openAssignment = new Assignment();
        openAssignment.setState(AIConstant.ASSIGNMENT_STATE_PUBLISHED);
        Student student = new Student();

        Submission existing = new Submission();
        existing.setFileUrl("old-file.pdf");

        when(submissionRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(openAssignment));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));

        ApiMessageDto<Void> result = submissionController.update(form);

        assertThat(result.getResult()).isTrue();
        verify(fileService).deleteFile("old-file.pdf");
        verify(submissionRepository).save(existing);
    }

    @Test
    void update_whenFileUrlUnchanged_doesNotDeleteFile() {
        UpdateSubmissionForm form = new UpdateSubmissionForm();
        form.setId(10L);
        form.setAssignmentId(1L);
        form.setStudentId(2L);
        form.setFileUrl("same-file.pdf");

        Assignment openAssignment = new Assignment();
        openAssignment.setState(AIConstant.ASSIGNMENT_STATE_PUBLISHED);
        Student student = new Student();

        Submission existing = new Submission();
        existing.setFileUrl("same-file.pdf");

        when(submissionRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(openAssignment));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));

        ApiMessageDto<Void> result = submissionController.update(form);

        assertThat(result.getResult()).isTrue();
        verify(fileService, never()).deleteFile(any());
    }

    @Test
    void get_whenFound_returnsSubmissionDto() {
        Submission entity = new Submission();
        SubmissionDto dto = new SubmissionDto();

        when(submissionRepository.findById(5L)).thenReturn(Optional.of(entity));
        when(submissionMapper.fromEntityToSubmissionDto(entity)).thenReturn(dto);

        ApiMessageDto<SubmissionDto> result = submissionController.get(5L);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getData()).isSameAs(dto);
    }

    @Test
    void list_whenCalled_returnsPagedList() {
        SubmissionCriteria criteria = new SubmissionCriteria();
        Pageable pageable = PageRequest.of(0, 10);

        Submission entity = new Submission();
        List<Submission> content = Collections.singletonList(entity);
        Page<Submission> page = new PageImpl<>(content, pageable, 1);
        List<SubmissionDto> dtoList = Collections.singletonList(new SubmissionDto());

        when(submissionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(submissionMapper.fromEntityToSubmissionDtoList(content)).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<SubmissionDto>>> result = submissionController.list(criteria, pageable);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).isEqualTo(dtoList);
        assertThat(result.getData().getTotalElements()).isEqualTo(1L);
        assertThat(result.getData().getTotalPages()).isEqualTo(1);
    }

    @Test
    void delete_whenFound_deletesSubmission() {
        Submission entity = new Submission();

        when(submissionRepository.findById(7L)).thenReturn(Optional.of(entity));

        ApiMessageDto<Void> result = submissionController.delete(7L);

        assertThat(result.getResult()).isTrue();
        verify(submissionRepository).deleteById(7L);
        verify(fileService, never()).deleteFile(any());
    }

    @Test
    void delete_whenFileUrlPresent_deletesFile() {
        Submission entity = new Submission();
        entity.setFileUrl("submission-file.pdf");

        when(submissionRepository.findById(7L)).thenReturn(Optional.of(entity));

        ApiMessageDto<Void> result = submissionController.delete(7L);

        assertThat(result.getResult()).isTrue();
        verify(fileService).deleteFile("submission-file.pdf");
        verify(submissionRepository).deleteById(7L);
    }

    @Test
    void grade_whenSuccess_setsScoreFeedbackAndFlipsStateToGraded() {
        GradeSubmissionForm form = new GradeSubmissionForm();
        form.setId(3L);
        form.setScore(8.5);
        form.setFeedback("Good job");

        Submission existing = new Submission();

        when(submissionRepository.findById(3L)).thenReturn(Optional.of(existing));
        doAnswer(invocation -> {
            GradeSubmissionForm gradeForm = invocation.getArgument(0);
            Submission target = invocation.getArgument(1);
            target.setScore(gradeForm.getScore());
            target.setFeedback(gradeForm.getFeedback());
            return null;
        }).when(submissionMapper).updateEntityFromGradeForm(form, existing);

        ApiMessageDto<Void> result = submissionController.grade(form);

        assertThat(result.getResult()).isTrue();
        assertThat(existing.getScore()).isEqualTo(8.5);
        assertThat(existing.getFeedback()).isEqualTo("Good job");
        assertThat(existing.getState()).isEqualTo(1);
        verify(submissionMapper).updateEntityFromGradeForm(form, existing);
        verify(submissionRepository).save(existing);
    }

    @Test
    void grade_whenAlreadyGraded_throwsBadRequestExceptionAndChangesNoField() {
        GradeSubmissionForm form = new GradeSubmissionForm();
        form.setId(3L);
        form.setScore(9.0);
        form.setFeedback("New feedback");

        Submission existing = new Submission();
        existing.setState(1);
        existing.setScore(5.0);
        existing.setFeedback("old feedback");

        when(submissionRepository.findById(3L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> submissionController.grade(form))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.SUBMISSION_ERROR_ALREADY_GRADED);

        assertThat(existing.getScore()).isEqualTo(5.0);
        assertThat(existing.getFeedback()).isEqualTo("old feedback");
        verify(submissionMapper, never()).updateEntityFromGradeForm(any(), any());
        verify(submissionRepository, never()).save(any());
    }
}
