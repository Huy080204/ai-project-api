package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.classroom.ClassroomDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.classroom.ChangeClassroomStateForm;
import com.ai.api.form.classroom.CreateClassroomForm;
import com.ai.api.form.classroom.UpdateClassroomForm;
import com.ai.api.mapper.ClassroomMapper;
import com.ai.api.model.Classroom;
import com.ai.api.model.Course;
import com.ai.api.model.criteria.ClassroomCriteria;
import com.ai.api.repository.ClassroomRepository;
import com.ai.api.repository.ClassroomStudentRepository;
import com.ai.api.repository.CourseRepository;
import com.ai.api.repository.NotificationGroupRepository;
import com.ai.api.repository.RegistrationRepository;
import com.ai.api.service.FileService;
import com.ai.api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ClassroomController}, covering create (+ course-not-found rejection),
 * update, get-by-id (not-found), list (courseId filter), delete (state=0 guard),
 * auto-complete (courseId scope, reduced fields), public-list (forced state=1), and
 * change-state (valid + invalid transitions).
 */
@ExtendWith(MockitoExtension.class)
class ClassroomControllerTest {

    @Mock
    private ClassroomRepository classroomRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private ClassroomStudentRepository classroomStudentRepository;

    @Mock
    private NotificationGroupRepository notificationGroupRepository;

    @Mock
    private FileService fileService;

    @Mock
    private ClassroomMapper classroomMapper;

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private ClassroomController classroomController;

    private CreateClassroomForm createForm(Long courseId) {
        CreateClassroomForm form = new CreateClassroomForm();
        form.setCourseId(courseId);
        form.setStartDate(new Date());
        form.setEndDate(new Date());
        form.setPrice(BigDecimal.TEN);
        return form;
    }

    private UpdateClassroomForm updateForm(Long id, Long courseId) {
        UpdateClassroomForm form = new UpdateClassroomForm();
        form.setId(id);
        form.setCourseId(courseId);
        form.setStartDate(new Date());
        form.setEndDate(new Date());
        form.setPrice(BigDecimal.ONE);
        return form;
    }

    private ChangeClassroomStateForm changeStateForm(Long id, Integer state) {
        ChangeClassroomStateForm form = new ChangeClassroomStateForm();
        form.setId(id);
        form.setState(state);
        return form;
    }

    // ------------------------------------------------------------------ create

    @Test
    void shouldCreateClassroomSuccessfully() {
        // Arrange
        CreateClassroomForm form = createForm(5L);
        BindingResult bindingResult = mock(BindingResult.class);
        Course course = new Course();
        course.setId(5L);
        Classroom classroom = new Classroom();

        when(courseRepository.findById(5L)).thenReturn(Optional.of(course));
        when(classroomMapper.fromCreateClassroomFormToEntity(form)).thenReturn(classroom);

        // Act
        ApiMessageDto<ClassroomDto> result = classroomController.create(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(classroom.getCourse()).isEqualTo(course);
        verify(classroomRepository).save(classroom);
    }

    @Test
    void shouldThrowNotFoundWhenCreateClassroomCourseNotFound() {
        // Arrange
        CreateClassroomForm form = createForm(999L);
        BindingResult bindingResult = mock(BindingResult.class);
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> classroomController.create(form, bindingResult))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.COURSE_ERROR_NOT_FOUND));
        verify(classroomRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ update

    @Test
    void shouldUpdateClassroomSuccessfully() {
        // Arrange
        UpdateClassroomForm form = updateForm(10L, 5L);
        BindingResult bindingResult = mock(BindingResult.class);
        Classroom classroom = new Classroom();
        classroom.setId(10L);
        classroom.setState(AIConstant.CLASSROOM_STATE_PENDING);
        Course course = new Course();
        course.setId(5L);

        when(classroomRepository.findById(10L)).thenReturn(Optional.of(classroom));
        when(courseRepository.findById(5L)).thenReturn(Optional.of(course));

        // Act
        ApiMessageDto<Void> result = classroomController.update(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(classroom.getCourse()).isEqualTo(course);
        verify(classroomMapper).updateEntityFromForm(form, classroom);
        verify(classroomRepository).save(classroom);
    }

    @Test
    void shouldThrowBadRequestWhenUpdateClassroomStateNotPending() {
        // Arrange
        UpdateClassroomForm form = updateForm(11L, 5L);
        BindingResult bindingResult = mock(BindingResult.class);
        Classroom classroom = new Classroom();
        classroom.setId(11L);
        classroom.setState(AIConstant.CLASSROOM_STATE_ACTIVE);
        when(classroomRepository.findById(11L)).thenReturn(Optional.of(classroom));

        // Act + Assert
        assertThatThrownBy(() -> classroomController.update(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_ERROR_UNABLE_UPDATE));
        verify(classroomRepository, never()).save(any());
    }

    // --------------------------------------------------------------------- get

    @Test
    void shouldThrowNotFoundWhenGetClassroomByIdNotFound() {
        // Arrange
        when(classroomRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> classroomController.get(99L))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_ERROR_NOT_FOUND));
    }

    // -------------------------------------------------------------------- list

    @Test
    @SuppressWarnings("unchecked")
    void shouldListClassroomsWithCourseIdFilter() {
        // Arrange
        ClassroomCriteria criteria = new ClassroomCriteria();
        criteria.setCourseId(5L);
        Pageable pageable = PageRequest.of(0, 10);

        Classroom classroom1 = new Classroom();
        classroom1.setId(1L);
        Classroom classroom2 = new Classroom();
        classroom2.setId(2L);
        Page<Classroom> page = new PageImpl<>(Arrays.asList(classroom1, classroom2), pageable, 2);

        ClassroomDto dto1 = new ClassroomDto();
        dto1.setId(1L);
        ClassroomDto dto2 = new ClassroomDto();
        dto2.setId(2L);

        when(classroomRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(classroomMapper.fromEntityToClassroomDtoList(anyList())).thenReturn(Arrays.asList(dto1, dto2));

        // Act
        ApiMessageDto<ResponseListDto<List<ClassroomDto>>> result = classroomController.list(criteria, pageable);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).hasSize(2);
        verify(classroomRepository).findAll(any(Specification.class), eq(pageable));
    }

    // ------------------------------------------------------------------ delete

    @Test
    void shouldThrowNotFoundWhenDeleteClassroomNotFound() {
        // Arrange
        when(classroomRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> classroomController.delete(99L))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_ERROR_NOT_FOUND));
        verify(classroomRepository, never()).deleteById(any());
    }

    @Test
    void shouldDeleteClassroomSuccessfullyWhenStatePending() {
        // Arrange
        Classroom classroom = new Classroom();
        classroom.setId(10L);
        classroom.setState(AIConstant.CLASSROOM_STATE_PENDING);
        when(classroomRepository.findById(10L)).thenReturn(Optional.of(classroom));
        when(notificationGroupRepository.findAvatarsByClassroomId(10L))
                .thenReturn(Collections.singletonList("/avatar/notification-group.png"));

        // Act
        ApiMessageDto<Void> result = classroomController.delete(10L);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(classroomRepository).deleteById(10L);
        InOrder inOrder = inOrder(registrationRepository, classroomRepository);
        inOrder.verify(registrationRepository).deleteAllByClassroomId(10L);
        inOrder.verify(classroomRepository).deleteById(10L);

        InOrder classroomStudentInOrder = inOrder(classroomStudentRepository, classroomRepository);
        classroomStudentInOrder.verify(classroomStudentRepository).deleteAllByClassroomId(10L);
        classroomStudentInOrder.verify(classroomRepository).deleteById(10L);

        // FR-009/FR-010: NotificationGroup rows scoped to this classroom must be cascade-deleted,
        // and their avatars batched into fileService.deleteFiles(...) before the classroom row
        // itself is removed.
        InOrder notificationGroupInOrder = inOrder(notificationGroupRepository, classroomRepository);
        notificationGroupInOrder.verify(notificationGroupRepository).deleteAllByClassroomId(10L);
        notificationGroupInOrder.verify(classroomRepository).deleteById(10L);
        verify(fileService).deleteFiles(Collections.singletonList("/avatar/notification-group.png"));
    }

    @Test
    void shouldDeleteClassroomSuccessfullyWhenStateNotPending() {
        // Arrange - deletion is no longer blocked by state (business rule dropped)
        Classroom classroom = new Classroom();
        classroom.setId(11L);
        classroom.setState(AIConstant.CLASSROOM_STATE_ACTIVE);
        when(classroomRepository.findById(11L)).thenReturn(Optional.of(classroom));

        // Act
        ApiMessageDto<Void> result = classroomController.delete(11L);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(classroomRepository).deleteById(11L);
        verify(registrationRepository).deleteAllByClassroomId(11L);
        verify(classroomStudentRepository).deleteAllByClassroomId(11L);
    }

    // ------------------------------------------------------------ auto-complete

    @Test
    @SuppressWarnings("unchecked")
    void shouldAutoCompleteClassroomsScopedByCourseIdWithReducedFields() {
        // Arrange
        ClassroomCriteria criteria = new ClassroomCriteria();
        criteria.setCourseId(5L);

        Classroom classroom = new Classroom();
        classroom.setId(1L);
        Page<Classroom> page = new PageImpl<>(Collections.singletonList(classroom), PageRequest.of(0, 10), 1);

        Course course = new Course();
        course.setId(5L);
        ClassroomDto dto = new ClassroomDto();
        dto.setId(1L);
        dto.setCourse(new com.ai.api.dto.course.CourseDto());
        // startDate/endDate/price intentionally left null - reduced-field auto-complete mapping.

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(classroomRepository.findAll(any(Specification.class), pageableCaptor.capture())).thenReturn(page);
        when(classroomMapper.fromEntityToClassroomDtoAutoCompleteList(anyList())).thenReturn(Collections.singletonList(dto));

        // Act
        ApiMessageDto<ResponseListDto<List<ClassroomDto>>> result = classroomController.autoComplete(criteria);

        // Assert
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).hasSize(1);
        assertThat(result.getData().getContent().get(0).getStartDate()).isNull();
        assertThat(result.getData().getContent().get(0).getEndDate()).isNull();
        assertThat(result.getData().getContent().get(0).getPrice()).isNull();
        verify(classroomMapper).fromEntityToClassroomDtoAutoCompleteList(anyList());
    }

    // -------------------------------------------------------------- public-list

    @Test
    @SuppressWarnings("unchecked")
    void shouldPublicListForceActiveStateRegardlessOfRequestedFilter() {
        // Arrange
        ClassroomCriteria criteria = new ClassroomCriteria();
        criteria.setState(AIConstant.CLASSROOM_STATE_PENDING);
        Pageable pageable = PageRequest.of(0, 10);

        Classroom classroom = new Classroom();
        classroom.setId(1L);
        classroom.setState(AIConstant.CLASSROOM_STATE_ACTIVE);
        Page<Classroom> page = new PageImpl<>(Collections.singletonList(classroom), pageable, 1);

        ClassroomDto dto = new ClassroomDto();
        dto.setId(1L);
        dto.setState(AIConstant.CLASSROOM_STATE_ACTIVE);

        when(classroomRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(classroomMapper.fromEntityToClassroomDtoList(anyList())).thenReturn(Collections.singletonList(dto));

        // Act
        ApiMessageDto<ResponseListDto<List<ClassroomDto>>> result = classroomController.publicList(criteria, pageable);

        // Assert
        assertThat(criteria.getState()).isEqualTo(AIConstant.CLASSROOM_STATE_ACTIVE);
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).hasSize(1);
        verify(classroomRepository).findAll(any(Specification.class), eq(pageable));
    }

    // ------------------------------------------------------------- change-state

    @Test
    void shouldChangeStateForValidTransitionPendingToActive() {
        // Arrange
        ChangeClassroomStateForm form = changeStateForm(10L, AIConstant.CLASSROOM_STATE_ACTIVE);
        BindingResult bindingResult = mock(BindingResult.class);
        Classroom classroom = new Classroom();
        classroom.setId(10L);
        classroom.setState(AIConstant.CLASSROOM_STATE_PENDING);
        when(classroomRepository.findById(10L)).thenReturn(Optional.of(classroom));

        // Act
        ApiMessageDto<Void> result = classroomController.changeState(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(classroom.getState()).isEqualTo(AIConstant.CLASSROOM_STATE_ACTIVE);
        verify(classroomRepository).save(classroom);
    }

    @Test
    void shouldThrowBadRequestWhenChangeStateSkipsAStep() {
        // Arrange - pending(0) -> done(2) is not a permitted transition
        ChangeClassroomStateForm form = changeStateForm(10L, AIConstant.CLASSROOM_STATE_DONE);
        BindingResult bindingResult = mock(BindingResult.class);
        Classroom classroom = new Classroom();
        classroom.setId(10L);
        classroom.setState(AIConstant.CLASSROOM_STATE_PENDING);
        when(classroomRepository.findById(10L)).thenReturn(Optional.of(classroom));

        // Act + Assert
        assertThatThrownBy(() -> classroomController.changeState(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_ERROR_INVALID_STATE_TRANSITION));
        assertThat(classroom.getState()).isEqualTo(AIConstant.CLASSROOM_STATE_PENDING);
        verify(classroomRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenChangeStateIsNoOp() {
        // Arrange - active(1) -> active(1) is not a permitted transition
        ChangeClassroomStateForm form = changeStateForm(10L, AIConstant.CLASSROOM_STATE_ACTIVE);
        BindingResult bindingResult = mock(BindingResult.class);
        Classroom classroom = new Classroom();
        classroom.setId(10L);
        classroom.setState(AIConstant.CLASSROOM_STATE_ACTIVE);
        when(classroomRepository.findById(10L)).thenReturn(Optional.of(classroom));

        // Act + Assert
        assertThatThrownBy(() -> classroomController.changeState(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_ERROR_INVALID_STATE_TRANSITION));
        verify(classroomRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenChangeStateAttemptsOutOfTerminalDone() {
        // Arrange - done(2) is terminal, no transition out is permitted
        ChangeClassroomStateForm form = changeStateForm(10L, AIConstant.CLASSROOM_STATE_CANCEL);
        BindingResult bindingResult = mock(BindingResult.class);
        Classroom classroom = new Classroom();
        classroom.setId(10L);
        classroom.setState(AIConstant.CLASSROOM_STATE_DONE);
        when(classroomRepository.findById(10L)).thenReturn(Optional.of(classroom));

        // Act + Assert
        assertThatThrownBy(() -> classroomController.changeState(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_ERROR_INVALID_STATE_TRANSITION));
        verify(classroomRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenChangeStateAttemptsOutOfTerminalCancel() {
        // Arrange - cancel(3) is terminal, no transition out is permitted
        ChangeClassroomStateForm form = changeStateForm(10L, AIConstant.CLASSROOM_STATE_ACTIVE);
        BindingResult bindingResult = mock(BindingResult.class);
        Classroom classroom = new Classroom();
        classroom.setId(10L);
        classroom.setState(AIConstant.CLASSROOM_STATE_CANCEL);
        when(classroomRepository.findById(10L)).thenReturn(Optional.of(classroom));

        // Act + Assert
        assertThatThrownBy(() -> classroomController.changeState(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_ERROR_INVALID_STATE_TRANSITION));
        verify(classroomRepository, never()).save(any());
    }
}
