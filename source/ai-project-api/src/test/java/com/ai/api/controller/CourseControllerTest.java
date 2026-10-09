package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.course.CourseDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.course.CreateCourseForm;
import com.ai.api.form.course.UpdateCourseForm;
import com.ai.api.mapper.CourseMapper;
import com.ai.api.model.Course;
import com.ai.api.model.criteria.CourseCriteria;
import com.ai.api.repository.ClassroomRepository;
import com.ai.api.repository.ClassroomStudentRepository;
import com.ai.api.repository.CourseRepository;
import com.ai.api.repository.RatingRepository;
import com.ai.api.repository.RegistrationRepository;
import com.ai.api.repository.SyllabusRepository;
import com.ai.api.service.FileService;
import com.ai.api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
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
 * Unit test for {@link CourseController}, covering create/update/get/list/delete, the
 * uniqueness checks (existsByName-based), avatar-file cleanup on update/delete, and the
 * auto-complete endpoint.
 */
@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    @Mock
    private CourseRepository courseRepository;

    @Spy
    private CourseMapper courseMapper = Mappers.getMapper(CourseMapper.class);

    @Mock
    private FileService fileService;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private ClassroomRepository classroomRepository;

    @Mock
    private ClassroomStudentRepository classroomStudentRepository;

    @Mock
    private SyllabusRepository syllabusRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private CourseController courseController;

    private CreateCourseForm createForm(String name) {
        CreateCourseForm form = new CreateCourseForm();
        form.setName(name);
        form.setAvatar("avatar.png");
        form.setPrice(BigDecimal.TEN);
        form.setShortDescription("short description");
        return form;
    }

    private UpdateCourseForm updateForm(Long id, String name) {
        UpdateCourseForm form = new UpdateCourseForm();
        form.setId(id);
        form.setName(name);
        form.setAvatar("avatar.png");
        form.setPrice(BigDecimal.ONE);
        form.setShortDescription("short description");
        return form;
    }

    // ------------------------------------------------------------------ create

    @Test
    void shouldCreateCourseSuccessfully() {
        // Arrange
        CreateCourseForm form = createForm("Java Basics");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        when(courseRepository.existsByName("Java Basics")).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // Act
        ApiMessageDto<CourseDto> result = courseController.create(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getId()).isEqualTo(1L);
        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(courseCaptor.capture());
        Course saved = courseCaptor.getValue();
        assertThat(saved.getName()).isEqualTo("Java Basics");
        assertThat(saved.getAvatar()).isEqualTo("avatar.png");
        assertThat(saved.getPrice()).isEqualTo(BigDecimal.TEN);
        assertThat(saved.getShortDescription()).isEqualTo("short description");
    }

    @Test
    void shouldThrowBadRequestWhenCreateCourseNameExists() {
        // Arrange
        CreateCourseForm form = createForm("Java Basics");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        when(courseRepository.existsByName("Java Basics")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> courseController.create(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.COURSE_ERROR_NAME_EXIST));
        verify(courseRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ update

    @Test
    void shouldUpdateCourseSuccessfully() {
        // Arrange
        UpdateCourseForm form = updateForm(1L, "Java Advanced");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        Course course = new Course();
        course.setId(1L);
        course.setName("Java Basics");
        course.setAvatar("avatar.png");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.existsByNameAndIdNot("Java Advanced", 1L)).thenReturn(false);

        // Act
        ApiMessageDto<Void> result = courseController.update(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(course.getName()).isEqualTo("Java Advanced");
        assertThat(course.getPrice()).isEqualTo(BigDecimal.ONE);
        assertThat(course.getShortDescription()).isEqualTo("short description");
        verify(courseRepository).save(course);
    }

    @Test
    void shouldThrowBadRequestWhenUpdateCourseNameExistsExcludingSelf() {
        // Arrange
        UpdateCourseForm form = updateForm(1L, "Java Advanced");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        Course course = new Course();
        course.setId(1L);
        course.setName("Java Basics");
        course.setAvatar("avatar.png");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.existsByNameAndIdNot("Java Advanced", 1L)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> courseController.update(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.COURSE_ERROR_NAME_EXIST));
        verify(courseRepository, never()).save(any());
        verify(fileService, never()).deleteFile(any());
    }

    @Test
    void shouldDeleteOldAvatarWhenUpdateAvatarChanges() {
        // Arrange
        UpdateCourseForm form = updateForm(1L, "Java Advanced");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        form.setAvatar("/avatar/new.png");
        Course course = new Course();
        course.setId(1L);
        course.setName("Java Basics");
        course.setAvatar("/avatar/old.png");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.existsByNameAndIdNot("Java Advanced", 1L)).thenReturn(false);

        // Act
        courseController.update(form, bindingResult);

        // Assert
        verify(fileService).deleteFile("/avatar/old.png");
    }

    @Test
    void shouldNotDeleteOldAvatarWhenUpdateAvatarUnchanged() {
        // Arrange
        UpdateCourseForm form = updateForm(1L, "Java Advanced");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        form.setAvatar("/avatar/same.png");
        Course course = new Course();
        course.setId(1L);
        course.setName("Java Basics");
        course.setAvatar("/avatar/same.png");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.existsByNameAndIdNot("Java Advanced", 1L)).thenReturn(false);

        // Act
        courseController.update(form, bindingResult);

        // Assert
        verify(fileService, never()).deleteFile(any());
    }

    // --------------------------------------------------------------------- get

    @Test
    void shouldThrowNotFoundWhenGetCourseByIdNotFound() {
        // Arrange
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> courseController.get(99L))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.COURSE_ERROR_NOT_FOUND));
    }

    // -------------------------------------------------------------------- list

    @Test
    @SuppressWarnings("unchecked")
    void shouldListCoursesWithNameFilter() {
        // Arrange
        CourseCriteria criteria = new CourseCriteria();
        criteria.setName("Java");
        Pageable pageable = PageRequest.of(0, 10);

        Course course = new Course();
        course.setId(1L);
        course.setName("Java Basics");
        Page<Course> page = new PageImpl<>(Collections.singletonList(course), pageable, 1);

        when(courseRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        ApiMessageDto<ResponseListDto<List<CourseDto>>> result = courseController.list(criteria, pageable);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).hasSize(1);
        assertThat(result.getData().getContent().get(0).getName()).isEqualTo("Java Basics");
        verify(courseRepository).findAll(any(Specification.class), eq(pageable));
    }

    // ------------------------------------------------------------------ delete

    @Test
    void shouldThrowNotFoundWhenDeleteCourseNotFound() {
        // Arrange
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> courseController.delete(99L))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.COURSE_ERROR_NOT_FOUND));
        verify(courseRepository, never()).deleteById(any());
        verify(fileService, never()).deleteFiles(any());
        verify(classroomRepository, never()).deleteAllByCourseId(any());
        verify(syllabusRepository, never()).deleteAllByCourseId(any());
        verify(registrationRepository, never()).deleteAllByClassroomCourseId(any());
        verify(ratingRepository, never()).deleteAllByCourseId(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDeleteAvatarFileWhenDeletingCourseWithNonBlankAvatar() {
        // Arrange
        Course course = new Course();
        course.setId(1L);
        course.setAvatar("/avatar/to-delete.png");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(syllabusRepository.findAvatarsByCourseId(1L)).thenReturn(Collections.emptyList());

        // Act
        ApiMessageDto<Void> result = courseController.delete(1L);

        // Assert
        assertThat(result.getResult()).isTrue();
        ArgumentCaptor<List<String>> filesCaptor = ArgumentCaptor.forClass(List.class);
        verify(fileService).deleteFiles(filesCaptor.capture());
        assertThat(filesCaptor.getValue()).containsExactly("/avatar/to-delete.png");
        verify(courseRepository).deleteById(1L);

        InOrder inOrder = inOrder(registrationRepository, classroomStudentRepository, classroomRepository, syllabusRepository, courseRepository);
        inOrder.verify(registrationRepository).deleteAllByClassroomCourseId(1L);
        inOrder.verify(classroomStudentRepository).deleteAllByClassroomCourseId(1L);
        inOrder.verify(classroomRepository).deleteAllByCourseId(1L);
        inOrder.verify(syllabusRepository).deleteAllByCourseId(1L);
        inOrder.verify(courseRepository).deleteById(1L);

        InOrder ratingInOrder = inOrder(ratingRepository, courseRepository);
        ratingInOrder.verify(ratingRepository).deleteAllByCourseId(1L);
        ratingInOrder.verify(courseRepository).deleteById(1L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCollectCourseAndSyllabusAvatarsWhenDeletingCourse() {
        // Arrange - the course avatar plus every syllabus avatar under it are collected into
        // one list and deleted via a single fileService.deleteFiles(...) batch call.
        Course course = new Course();
        course.setId(1L);
        course.setAvatar("/avatar/course.png");
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(syllabusRepository.findAvatarsByCourseId(1L))
                .thenReturn(Arrays.asList("/avatar/chapter1.png", "/avatar/chapter2.png"));

        // Act
        courseController.delete(1L);

        // Assert
        ArgumentCaptor<List<String>> filesCaptor = ArgumentCaptor.forClass(List.class);
        verify(fileService).deleteFiles(filesCaptor.capture());
        assertThat(filesCaptor.getValue()).containsExactly(
                "/avatar/course.png", "/avatar/chapter1.png", "/avatar/chapter2.png");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotDeleteFileWhenDeletingCourseWithBlankAvatar() {
        // Arrange
        Course course = new Course();
        course.setId(1L);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(syllabusRepository.findAvatarsByCourseId(1L)).thenReturn(Collections.emptyList());

        // Act
        courseController.delete(1L);

        // Assert
        ArgumentCaptor<List<String>> filesCaptor = ArgumentCaptor.forClass(List.class);
        verify(fileService).deleteFiles(filesCaptor.capture());
        assertThat(filesCaptor.getValue()).isEmpty();
        verify(courseRepository).deleteById(1L);
    }

    // ------------------------------------------------------------ auto-complete

    @Test
    @SuppressWarnings("unchecked")
    void shouldAutoCompleteCoursesScopedToActiveStatusWithIdNameAvatarOnly() {
        // Arrange
        CourseCriteria criteria = new CourseCriteria();
        criteria.setName("Java");

        Course course = new Course();
        course.setId(1L);
        course.setName("Java Basics");
        course.setAvatar("/avatar/java.png");
        Page<Course> page = new PageImpl<>(Collections.singletonList(course), PageRequest.of(0, 10), 1);

        course.setPrice(BigDecimal.TEN);

        when(courseRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // Act
        ApiMessageDto<ResponseListDto<List<CourseDto>>> result = courseController.autoComplete(criteria);

        // Assert
        assertThat(criteria.getStatus()).isEqualTo(AIConstant.STATUS_ACTIVE);
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).hasSize(1);
        assertThat(result.getData().getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getData().getContent().get(0).getName()).isEqualTo("Java Basics");
        assertThat(result.getData().getContent().get(0).getAvatar()).isEqualTo("/avatar/java.png");
        assertThat(result.getData().getContent().get(0).getPrice()).isNull();
    }
}
