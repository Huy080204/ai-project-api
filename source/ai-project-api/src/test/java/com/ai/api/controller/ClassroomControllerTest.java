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
import com.ai.api.mapper.CourseMapper;
import com.ai.api.model.Classroom;
import com.ai.api.model.Course;
import com.ai.api.model.criteria.ClassroomCriteria;
import com.ai.api.repository.ClassroomRepository;
import com.ai.api.repository.ClassroomStudentRepository;
import com.ai.api.repository.CourseRepository;
import com.ai.api.repository.RegistrationRepository;
import com.ai.api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
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

    @Spy
    private ClassroomMapper classroomMapper = Mappers.getMapper(ClassroomMapper.class);

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private ClassroomController classroomController;

    @BeforeEach
    void setUp() {
        // ClassroomMapper delegates course to CourseMapper (`uses = {...}`); the generated impl
        // @Autowired-injects it, which Mappers.getMapper(...) does not do.
        ReflectionTestUtils.setField(classroomMapper, "courseMapper", Mappers.getMapper(CourseMapper.class));
    }

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
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        Course course = new Course();
        course.setId(5L);

        when(courseRepository.findById(5L)).thenReturn(Optional.of(course));
        when(classroomRepository.save(any(Classroom.class))).thenAnswer(invocation -> {
            Classroom saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        // Act
        ApiMessageDto<ClassroomDto> result = classroomController.create(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getId()).isEqualTo(10L);
        ArgumentCaptor<Classroom> classroomCaptor = ArgumentCaptor.forClass(Classroom.class);
        verify(classroomRepository).save(classroomCaptor.capture());
        Classroom saved = classroomCaptor.getValue();
        assertThat(saved.getCourse()).isSameAs(course);
        assertThat(saved.getPrice()).isEqualTo(BigDecimal.TEN);
        assertThat(saved.getStartDate()).isEqualTo(form.getStartDate());
        assertThat(saved.getEndDate()).isEqualTo(form.getEndDate());
    }

    @Test
    void shouldThrowNotFoundWhenCreateClassroomCourseNotFound() {
        // Arrange
        CreateClassroomForm form = createForm(999L);
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
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
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
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
        assertThat(classroom.getPrice()).isEqualTo(BigDecimal.ONE);
        assertThat(classroom.getStartDate()).isEqualTo(form.getStartDate());
        assertThat(classroom.getEndDate()).isEqualTo(form.getEndDate());
        verify(classroomRepository).save(classroom);
    }

    @Test
    void shouldThrowBadRequestWhenUpdateClassroomStateNotPending() {
        // Arrange
        UpdateClassroomForm form = updateForm(11L, 5L);
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
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

        when(classroomRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        ApiMessageDto<ResponseListDto<List<ClassroomDto>>> result = classroomController.list(criteria, pageable);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).extracting(ClassroomDto::getId).containsExactly(1L, 2L);
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
    }

    @Test
    void shouldThrowBadRequestWhenDeleteClassroomStateNotPending() {
        // Arrange
        Classroom classroom = new Classroom();
        classroom.setId(11L);
        classroom.setState(AIConstant.CLASSROOM_STATE_ACTIVE);
        when(classroomRepository.findById(11L)).thenReturn(Optional.of(classroom));

        // Act + Assert
        assertThatThrownBy(() -> classroomController.delete(11L))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_ERROR_UNABLE_DELETE));
        verify(classroomRepository, never()).deleteById(any());
        verify(registrationRepository, never()).deleteAllByClassroomId(any());
        verify(classroomStudentRepository, never()).deleteAllByClassroomId(any());
    }

    // ------------------------------------------------------------ auto-complete

    @Test
    @SuppressWarnings("unchecked")
    void shouldAutoCompleteClassroomsScopedByCourseIdWithReducedFields() {
        // Arrange
        ClassroomCriteria criteria = new ClassroomCriteria();
        criteria.setCourseId(5L);

        // startDate/endDate/price are set on the entity, so the null assertions below prove the
        // auto-complete mapping really leaves them out.
        Classroom classroom = new Classroom();
        classroom.setId(1L);
        classroom.setStartDate(new Date());
        classroom.setEndDate(new Date());
        classroom.setPrice(BigDecimal.TEN);
        Page<Classroom> page = new PageImpl<>(Collections.singletonList(classroom), PageRequest.of(0, 10), 1);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(classroomRepository.findAll(any(Specification.class), pageableCaptor.capture())).thenReturn(page);

        // Act
        ApiMessageDto<ResponseListDto<List<ClassroomDto>>> result = classroomController.autoComplete(criteria);

        // Assert
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).hasSize(1);
        assertThat(result.getData().getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getData().getContent().get(0).getStartDate()).isNull();
        assertThat(result.getData().getContent().get(0).getEndDate()).isNull();
        assertThat(result.getData().getContent().get(0).getPrice()).isNull();
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

        when(classroomRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        ApiMessageDto<ResponseListDto<List<ClassroomDto>>> result = classroomController.publicList(criteria, pageable);

        // Assert
        assertThat(criteria.getState()).isEqualTo(AIConstant.CLASSROOM_STATE_ACTIVE);
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).hasSize(1);
        assertThat(result.getData().getContent().get(0).getState()).isEqualTo(AIConstant.CLASSROOM_STATE_ACTIVE);
        verify(classroomRepository).findAll(any(Specification.class), eq(pageable));
    }

    // ------------------------------------------------------------- change-state

    @Test
    void shouldChangeStateForValidTransitionPendingToActive() {
        // Arrange
        ChangeClassroomStateForm form = changeStateForm(10L, AIConstant.CLASSROOM_STATE_ACTIVE);
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
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
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
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
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
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
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
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
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
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
