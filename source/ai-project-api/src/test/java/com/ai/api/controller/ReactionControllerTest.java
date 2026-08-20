package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.reaction.ReactionDto;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.reaction.CreateReactionForm;
import com.ai.api.form.reaction.UpdateReactionForm;
import com.ai.api.mapper.ReactionMapper;
import com.ai.api.model.Course;
import com.ai.api.model.Reaction;
import com.ai.api.model.Student;
import com.ai.api.model.criteria.ReactionCriteria;
import com.ai.api.repository.CourseRepository;
import com.ai.api.repository.ReactionRepository;
import com.ai.api.repository.StudentRepository;
import com.ai.api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReactionControllerTest {

    @Mock
    private ReactionRepository reactionRepository;
    @Mock
    private ReactionMapper reactionMapper;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private ReactionController controller;

    private CreateReactionForm buildCreateForm() {
        CreateReactionForm form = new CreateReactionForm();
        form.setCourseId(1L);
        form.setStudentId(2L);
        form.setContent("nice course");
        form.setType(AIConstant.REACTION_TYPE_LIKE);
        return form;
    }

    private UpdateReactionForm buildUpdateForm() {
        UpdateReactionForm form = new UpdateReactionForm();
        form.setId(10L);
        form.setContent("updated content");
        form.setType(AIConstant.REACTION_TYPE_DISLIKE);
        return form;
    }

    @Test
    void shouldCreateReactionWhenCourseAndStudentExistAndNoDuplicate() {
        CreateReactionForm form = buildCreateForm();
        Course course = new Course();
        course.setId(1L);
        Student student = new Student();
        student.setId(2L);
        Reaction entity = new Reaction();
        ReactionDto dto = new ReactionDto();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(reactionRepository.findByCourseIdAndStudentIdAndStatus(1L, 2L, AIConstant.STATUS_ACTIVE))
                .thenReturn(null);
        when(reactionMapper.fromFormToEntity(form)).thenReturn(entity);
        when(reactionRepository.save(entity)).thenReturn(entity);
        when(reactionMapper.fromEntityToReactionIdDto(entity)).thenReturn(dto);

        ApiMessageDto<ReactionDto> result = controller.create(form);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getData()).isSameAs(dto);
        verify(reactionRepository).save(entity);
    }

    @Test
    void shouldThrowNotFoundWhenCreateCourseDoesNotExist() {
        CreateReactionForm form = buildCreateForm();
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.create(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.REACTION_ERROR_COURSE_NOT_FOUND);
    }

    @Test
    void shouldThrowNotFoundWhenCreateStudentDoesNotExist() {
        CreateReactionForm form = buildCreateForm();
        Course course = new Course();
        course.setId(1L);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(studentRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.create(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.REACTION_ERROR_STUDENT_NOT_FOUND);
    }

    @Test
    void shouldThrowDuplicateWhenActiveReactionAlreadyExistsForCourseAndStudent() {
        CreateReactionForm form = buildCreateForm();
        Course course = new Course();
        course.setId(1L);
        Student student = new Student();
        student.setId(2L);
        Reaction existing = new Reaction();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(reactionRepository.findByCourseIdAndStudentIdAndStatus(1L, 2L, AIConstant.STATUS_ACTIVE))
                .thenReturn(existing);

        assertThatThrownBy(() -> controller.create(form))
                .isInstanceOf(com.ai.api.exception.BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.REACTION_ERROR_DUPLICATE);
    }

    @Test
    void shouldUpdateReactionWhenIdExists() {
        UpdateReactionForm form = buildUpdateForm();
        Reaction entity = new Reaction();
        ReactionDto dto = new ReactionDto();

        when(reactionRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(reactionRepository.save(entity)).thenReturn(entity);
        when(reactionMapper.fromEntityToReactionDto(entity)).thenReturn(dto);

        ApiMessageDto<ReactionDto> result = controller.update(form);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getData()).isSameAs(dto);
        verify(reactionMapper).updateEntityFromForm(form, entity);
        verify(reactionRepository).save(entity);
    }

    @Test
    void shouldThrowNotFoundWhenUpdateReactionIdDoesNotExist() {
        UpdateReactionForm form = buildUpdateForm();
        when(reactionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.update(form))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.REACTION_ERROR_NOT_FOUND);
    }

    @Test
    void shouldReturnReactionDtoWhenGetIdExists() {
        Reaction entity = new Reaction();
        ReactionDto dto = new ReactionDto();
        when(reactionRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(reactionMapper.fromEntityToReactionDto(entity)).thenReturn(dto);

        ApiMessageDto<ReactionDto> result = controller.get(10L);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getData()).isSameAs(dto);
    }

    @Test
    void shouldThrowNotFoundWhenGetReactionIdDoesNotExist() {
        when(reactionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.get(10L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.REACTION_ERROR_NOT_FOUND);
    }

    @Test
    void shouldDeleteReactionWhenIdExists() {
        Reaction entity = new Reaction();
        when(reactionRepository.findById(10L)).thenReturn(Optional.of(entity));

        ApiMessageDto<Void> result = controller.delete(10L);

        assertThat(result.getResult()).isTrue();
        verify(reactionRepository).deleteById(10L);
    }

    @Test
    void shouldThrowNotFoundWhenDeleteReactionIdDoesNotExist() {
        when(reactionRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.delete(10L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.REACTION_ERROR_NOT_FOUND);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldListReactionsFilteredByCourseIdStudentIdAndType() {
        ReactionCriteria criteria = new ReactionCriteria();
        criteria.setCourseId(1L);
        criteria.setStudentId(2L);
        criteria.setType(AIConstant.REACTION_TYPE_LIKE);
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        Reaction entity = new Reaction();
        Page<Reaction> page = new PageImpl<>(Collections.singletonList(entity));
        List<ReactionDto> dtoList = Collections.singletonList(new ReactionDto());

        when(reactionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(reactionMapper.fromEntityToReactionDtoList(any(List.class))).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<ReactionDto>>> result = controller.list(criteria, pageable);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).isEqualTo(dtoList);
        assertThat(result.getData().getTotalElements()).isEqualTo(1L);
        assertThat(result.getData().getTotalPages()).isEqualTo(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldForceActiveStatusAndSkipAuthWhenPublicList() {
        ReactionCriteria criteria = new ReactionCriteria();
        criteria.setCourseId(1L);
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        Page<Reaction> page = new PageImpl<>(Collections.emptyList());
        List<ReactionDto> dtoList = Collections.emptyList();

        when(reactionRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(reactionMapper.fromEntityToReactionDtoPublicList(any(List.class))).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<ReactionDto>>> result = controller.publicList(criteria, pageable);

        assertThat(result.getResult()).isTrue();
        assertThat(criteria.getStatus()).isEqualTo(AIConstant.STATUS_ACTIVE);
        verify(userService, org.mockito.Mockito.never()).getAddInfoFromToken();
    }
}
