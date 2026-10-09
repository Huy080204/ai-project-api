package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.rating.RatingDto;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.rating.CreateRatingForm;
import com.ai.api.form.rating.UpdateRatingForm;
import com.ai.api.mapper.CourseMapper;
import com.ai.api.mapper.RatingMapper;
import com.ai.api.mapper.StudentMapper;
import com.ai.api.model.Course;
import com.ai.api.model.Rating;
import com.ai.api.model.Student;
import com.ai.api.model.criteria.RatingCriteria;
import com.ai.api.repository.CourseRepository;
import com.ai.api.repository.RatingRepository;
import com.ai.api.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link RatingController}.
 */
@ExtendWith(MockitoExtension.class)
class RatingControllerTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private StudentRepository studentRepository;

    @Spy
    private RatingMapper ratingMapper = Mappers.getMapper(RatingMapper.class);

    @InjectMocks
    private RatingController ratingController;

    @BeforeEach
    void setUp() {
        // RatingMapper delegates course/student to their own Mappers (`uses = {...}`); the
        // generated impl @Autowired-injects them, which Mappers.getMapper(...) does not do.
        ReflectionTestUtils.setField(ratingMapper, "courseMapper", Mappers.getMapper(CourseMapper.class));
        ReflectionTestUtils.setField(ratingMapper, "studentMapper", Mappers.getMapper(StudentMapper.class));
    }

    private CreateRatingForm createForm() {
        CreateRatingForm form = new CreateRatingForm();
        form.setCourseId(1L);
        form.setStudentId(2L);
        form.setMessage("Great course");
        form.setStar(5);
        return form;
    }

    private UpdateRatingForm updateForm(Long id) {
        UpdateRatingForm form = new UpdateRatingForm();
        form.setId(id);
        form.setMessage("Updated message");
        form.setStar(4);
        return form;
    }

    // ------------------------------------------------------------------ create

    @Test
    void shouldThrowNotFoundWhenCreateCourseMissing() {
        // Arrange
        CreateRatingForm form = createForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> ratingController.create(form, bindingResult))
                .isInstanceOf(NotFoundException.class);
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenCreateStudentMissing() {
        // Arrange
        CreateRatingForm form = createForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        Course course = new Course();
        course.setId(1L);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(studentRepository.findById(2L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> ratingController.create(form, bindingResult))
                .isInstanceOf(NotFoundException.class);
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void shouldCreateRatingSuccessfully() {
        // Arrange
        CreateRatingForm form = createForm();
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        Course course = new Course();
        course.setId(1L);
        Student student = new Student();
        student.setId(2L);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(ratingRepository.save(any(Rating.class))).thenAnswer(invocation -> {
            Rating saved = invocation.getArgument(0);
            saved.setId(3L);
            return saved;
        });

        // Act
        ApiMessageDto<RatingDto> result = ratingController.create(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getId()).isEqualTo(3L);
        ArgumentCaptor<Rating> ratingCaptor = ArgumentCaptor.forClass(Rating.class);
        verify(ratingRepository).save(ratingCaptor.capture());
        Rating saved = ratingCaptor.getValue();
        assertThat(saved.getMessage()).isEqualTo("Great course");
        assertThat(saved.getStar()).isEqualTo(5);
        assertThat(saved.getCourse()).isSameAs(course);
        assertThat(saved.getStudent()).isSameAs(student);
    }

    // ------------------------------------------------------------------ update

    @Test
    void shouldThrowNotFoundWhenUpdateIdMissing() {
        // Arrange
        UpdateRatingForm form = updateForm(1L);
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        when(ratingRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> ratingController.update(form, bindingResult))
                .isInstanceOf(NotFoundException.class);
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void shouldUpdateRatingSuccessfully() {
        // Arrange
        UpdateRatingForm form = updateForm(1L);
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        Rating rating = new Rating();
        rating.setId(1L);
        when(ratingRepository.findById(1L)).thenReturn(Optional.of(rating));

        // Act
        ApiMessageDto<Void> result = ratingController.update(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(rating.getMessage()).isEqualTo("Updated message");
        assertThat(rating.getStar()).isEqualTo(4);
        verify(ratingRepository).save(rating);
    }

    // --------------------------------------------------------------------- get

    @Test
    void shouldThrowNotFoundWhenGetIdMissing() {
        // Arrange
        when(ratingRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> ratingController.get(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldGetRatingSuccessfully() {
        // Arrange
        Rating rating = new Rating();
        rating.setId(1L);
        rating.setMessage("Great course");
        rating.setStar(5);

        when(ratingRepository.findById(1L)).thenReturn(Optional.of(rating));

        // Act
        ApiMessageDto<RatingDto> result = ratingController.get(1L);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getId()).isEqualTo(1L);
        assertThat(result.getData().getMessage()).isEqualTo("Great course");
        assertThat(result.getData().getStar()).isEqualTo(5);
    }

    // ------------------------------------------------------------------ delete

    @Test
    void shouldThrowNotFoundWhenDeleteIdMissing() {
        // Arrange
        when(ratingRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> ratingController.delete(1L))
                .isInstanceOf(NotFoundException.class);
        verify(ratingRepository, never()).deleteById(any());
    }

    @Test
    void shouldDeleteRatingSuccessfully() {
        // Arrange
        Rating rating = new Rating();
        rating.setId(1L);
        when(ratingRepository.findById(1L)).thenReturn(Optional.of(rating));

        // Act
        ApiMessageDto<Void> result = ratingController.delete(1L);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(ratingRepository).deleteById(1L);
    }

    // -------------------------------------------------------------------- list

    @Test
    @SuppressWarnings("unchecked")
    void shouldListRatingsWithCourseIdFilter() {
        // Arrange
        RatingCriteria criteria = new RatingCriteria();
        criteria.setCourseId(1L);
        Pageable pageable = PageRequest.of(0, 10);

        Rating rating = new Rating();
        rating.setId(1L);
        rating.setMessage("Great course");
        Page<Rating> page = new PageImpl<>(Collections.singletonList(rating), pageable, 1);

        when(ratingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        ApiMessageDto<ResponseListDto<List<RatingDto>>> result = ratingController.list(criteria, pageable);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).hasSize(1);
        assertThat(result.getData().getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getData().getContent().get(0).getMessage()).isEqualTo("Great course");
        verify(ratingRepository).findAll(any(Specification.class), eq(pageable));
    }

    // ------------------------------------------------------------- publicList

    @Test
    @SuppressWarnings("unchecked")
    void shouldForceActiveStatusOnPublicList() {
        // Arrange
        RatingCriteria criteria = new RatingCriteria();
        criteria.setCourseId(1L);
        Pageable pageable = PageRequest.of(0, 10);

        Rating rating = new Rating();
        rating.setId(1L);
        rating.setMessage("Great course");
        Page<Rating> page = new PageImpl<>(Collections.singletonList(rating), pageable, 1);

        when(ratingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        ApiMessageDto<ResponseListDto<List<RatingDto>>> result = ratingController.publicList(criteria, pageable);

        // Assert
        assertThat(criteria.getStatus()).isEqualTo(AIConstant.STATUS_ACTIVE);
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).hasSize(1);
        assertThat(result.getData().getContent().get(0).getMessage()).isEqualTo("Great course");
    }
}
