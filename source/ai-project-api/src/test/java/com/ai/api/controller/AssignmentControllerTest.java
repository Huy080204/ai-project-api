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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class AssignmentControllerTest {

    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private SyllabusRepository syllabusRepository;
    @Mock
    private AssignmentMapper assignmentMapper;
    @Mock
    private FileService fileService;
    @Mock
    private SubmissionRepository submissionRepository;
    @InjectMocks
    private AssignmentController controller;

    @Test
    void create_whenSyllabusFound_returnsSuccess() {
        CreateAssignmentForm form = new CreateAssignmentForm();
        form.setSyllabusId(1L);
        form.setTitle("Assignment 1");

        Syllabus syllabus = new Syllabus();
        Assignment entity = new Assignment();

        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(syllabus));
        when(assignmentMapper.fromFormToEntity(form)).thenReturn(entity);
        when(assignmentRepository.save(entity)).thenReturn(entity);

        ApiMessageDto<Void> result = controller.create(form);

        assertThat(result.getResult()).isTrue();
        verify(assignmentRepository).save(entity);
    }

    @Test
    void create_whenSyllabusNotFound_throwsNotFoundException() {
        CreateAssignmentForm form = new CreateAssignmentForm();
        form.setSyllabusId(99L);
        form.setTitle("Assignment 1");

        when(syllabusRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.create(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.SYLLABUS_ERROR_NOT_FOUND);
    }

    @Test
    void update_whenSuccess_updatesFieldsAndState() {
        UpdateAssignmentForm form = new UpdateAssignmentForm();
        form.setId(1L);
        form.setTitle("Updated title");
        form.setState(AIConstant.ASSIGNMENT_STATE_PUBLISHED);

        Assignment entity = new Assignment();
        entity.setState(AIConstant.ASSIGNMENT_STATE_DRAFT);

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(assignmentRepository.save(entity)).thenReturn(entity);

        ApiMessageDto<Void> result = controller.update(form);

        assertThat(result.getResult()).isTrue();
        verify(assignmentMapper).updateEntityFromForm(eq(form), eq(entity));
        verify(assignmentRepository).save(entity);
    }

    @Test
    void update_whenCurrentStateClosed_throwsBadRequestAndDoesNotChangeOrDeleteFile() {
        UpdateAssignmentForm form = new UpdateAssignmentForm();
        form.setId(1L);
        form.setTitle("Updated title");
        form.setFileAttachmentUrl("new-file.pdf");
        form.setState(AIConstant.ASSIGNMENT_STATE_PUBLISHED);

        Assignment entity = new Assignment();
        entity.setTitle("Original title");
        entity.setFileAttachmentUrl("old-file.pdf");
        entity.setState(AIConstant.ASSIGNMENT_STATE_CLOSED);

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> controller.update(form))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ASSIGNMENT_ERROR_UNABLE_UPDATE);

        assertThat(entity.getTitle()).isEqualTo("Original title");
        assertThat(entity.getFileAttachmentUrl()).isEqualTo("old-file.pdf");
        assertThat(entity.getState()).isEqualTo(AIConstant.ASSIGNMENT_STATE_CLOSED);
        verify(fileService, never()).deleteFile(any());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void update_whenFileAttachmentUrlChangedOnNonClosedRow_deletesOldFile() {
        UpdateAssignmentForm form = new UpdateAssignmentForm();
        form.setId(1L);
        form.setTitle("Updated title");
        form.setFileAttachmentUrl("new-file.pdf");
        form.setState(AIConstant.ASSIGNMENT_STATE_PUBLISHED);

        Assignment entity = new Assignment();
        entity.setFileAttachmentUrl("old-file.pdf");
        entity.setState(AIConstant.ASSIGNMENT_STATE_DRAFT);

        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(assignmentRepository.save(entity)).thenReturn(entity);

        controller.update(form);

        verify(fileService, times(1)).deleteFile("old-file.pdf");
    }

    @Test
    void update_whenNotFound_throwsNotFoundException() {
        UpdateAssignmentForm form = new UpdateAssignmentForm();
        form.setId(1L);
        form.setTitle("Updated title");
        form.setState(AIConstant.ASSIGNMENT_STATE_PUBLISHED);

        when(assignmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.update(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ASSIGNMENT_ERROR_NOT_FOUND);
    }

    @Test
    void get_whenFound_returnsSuccessDto() {
        Assignment entity = new Assignment();
        AssignmentDto dto = new AssignmentDto();
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(assignmentMapper.fromEntityToAssignmentDto(entity)).thenReturn(dto);

        ApiMessageDto<AssignmentDto> result = controller.get(1L);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getData()).isSameAs(dto);
    }

    @Test
    void get_whenNotFound_throwsNotFoundException() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.get(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ASSIGNMENT_ERROR_NOT_FOUND);
    }

    @Test
    void list_returnsResponseListDtoShape() {
        AssignmentCriteria criteria = new AssignmentCriteria();
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        Assignment entity = new Assignment();
        Page<Assignment> page = new PageImpl<>(Collections.singletonList(entity));
        List<AssignmentDto> dtoList = Collections.singletonList(new AssignmentDto());

        when(assignmentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(assignmentMapper.fromEntityToAssignmentDtoList(page.getContent())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<AssignmentDto>>> result = controller.list(criteria, pageable);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).isEqualTo(dtoList);
        assertThat(result.getData().getTotalElements()).isEqualTo(page.getTotalElements());
        assertThat(result.getData().getTotalPages()).isEqualTo(page.getTotalPages());
    }

    @Test
    void delete_whenFileAttachmentUrlPresent_deletesFileThenEntity() {
        Assignment entity = new Assignment();
        entity.setFileAttachmentUrl("old-file.pdf");
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(entity));

        controller.delete(1L);

        verify(fileService, times(1)).deleteFiles(Collections.singletonList("old-file.pdf"));
        verify(assignmentRepository).deleteById(1L);
    }

    @Test
    void delete_whenSubmissionChildrenExist_cascadeDeletesBeforeAssignment() {
        Assignment entity = new Assignment();
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(entity));

        controller.delete(1L);

        InOrder order = inOrder(submissionRepository, assignmentRepository);
        order.verify(submissionRepository).deleteAllByAssignmentId(1L);
        order.verify(assignmentRepository).deleteById(1L);
    }

    @Test
    void delete_whenSubmissionsHaveFileUrls_cleansUpFilesBeforeCascadeDelete() {
        Assignment entity = new Assignment();
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(submissionRepository.findFileUrlsByAssignmentId(1L))
                .thenReturn(Collections.singletonList("submission-file.pdf"));

        controller.delete(1L);

        InOrder order = inOrder(fileService, submissionRepository);
        order.verify(fileService).deleteFiles(Collections.singletonList("submission-file.pdf"));
        order.verify(submissionRepository).deleteAllByAssignmentId(1L);
    }

    @Test
    void delete_whenNoSubmissionFileUrls_doesNotCallDeleteFiles() {
        Assignment entity = new Assignment();
        when(assignmentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(submissionRepository.findFileUrlsByAssignmentId(1L)).thenReturn(Collections.emptyList());

        controller.delete(1L);

        verify(fileService, never()).deleteFiles(any());
    }

    @Test
    void delete_whenNotFound_throwsNotFoundException() {
        when(assignmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.ASSIGNMENT_ERROR_NOT_FOUND);
    }
}
