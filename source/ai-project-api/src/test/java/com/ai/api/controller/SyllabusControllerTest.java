package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.syllabus.SyllabusDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.syllabus.CreateSyllabusForm;
import com.ai.api.form.syllabus.UpdateSyllabusForm;
import com.ai.api.form.syllabus.UpdateSyllabusOrderingForm;
import com.ai.api.mapper.SyllabusMapper;
import com.ai.api.model.Course;
import com.ai.api.model.Syllabus;
import com.ai.api.model.criteria.SyllabusCriteria;
import com.ai.api.repository.CourseRepository;
import com.ai.api.repository.SyllabusRepository;
import com.ai.api.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link SyllabusController}, covering create/update/get/list/delete, the
 * chapter-timeline-required rule, course total-timeline recomputation, avatar-file cleanup on
 * update/delete, the update-ordering batch endpoint, and the unauthenticated publicList
 * endpoint.
 */
@ExtendWith(MockitoExtension.class)
class SyllabusControllerTest {

    @Mock
    private SyllabusRepository syllabusRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private SyllabusMapper syllabusMapper;

    @Mock
    private FileService fileService;

    @InjectMocks
    private SyllabusController syllabusController;

    private CreateSyllabusForm createForm(Long courseId, Integer kind, Integer timeline) {
        CreateSyllabusForm form = new CreateSyllabusForm();
        form.setCourseId(courseId);
        form.setKind(kind);
        form.setName("Intro");
        form.setAvatar("avatar.png");
        form.setDescription("description");
        form.setOrdering(1);
        form.setTimeline(timeline);
        return form;
    }

    private UpdateSyllabusForm updateForm(Long id, String avatar, Integer timeline) {
        UpdateSyllabusForm form = new UpdateSyllabusForm();
        form.setId(id);
        form.setName("Intro updated");
        form.setAvatar(avatar);
        form.setDescription("description updated");
        form.setOrdering(1);
        form.setTimeline(timeline);
        return form;
    }

    private Course course(Long id) {
        Course course = new Course();
        course.setId(id);
        course.setAvatar("course-avatar.png");
        return course;
    }

    // ------------------------------------------------------------------ create

    @Test
    void shouldCreateLessonSyllabusSuccessfullyWithoutTimeline() {
        // Arrange
        CreateSyllabusForm form = createForm(1L, AIConstant.SYLLABUS_KIND_LESSON, null);
        BindingResult bindingResult = mock(BindingResult.class);
        Course course = course(1L);
        Syllabus syllabus = new Syllabus();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(syllabusMapper.fromCreateSyllabusFormToEntity(form)).thenReturn(syllabus);
        when(syllabusRepository.sumTimelineByCourseIdAndKind(1L, AIConstant.SYLLABUS_KIND_CHAPTER)).thenReturn(0);

        // Act
        ApiMessageDto<Void> result = syllabusController.create(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(syllabus.getCourse()).isEqualTo(course);
        verify(syllabusRepository).save(syllabus);
    }

    @Test
    void shouldThrowNotFoundWhenCreateSyllabusWithInvalidCourseId() {
        // Arrange
        CreateSyllabusForm form = createForm(99L, AIConstant.SYLLABUS_KIND_LESSON, null);
        BindingResult bindingResult = mock(BindingResult.class);
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> syllabusController.create(form, bindingResult))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.COURSE_ERROR_NOT_FOUND));
        verify(syllabusRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWithoutErrorCodeWhenCreateChapterSyllabusWithNullTimeline() {
        // Arrange - deliberate exception to the usual ErrorCode rule: message-only constructor,
        // getCode() MUST be null for this one case. The kind/timeline check now runs BEFORE
        // the course lookup (saves a query on the common invalid-input path), so neither
        // courseRepository nor syllabusMapper is ever called here.
        CreateSyllabusForm form = createForm(1L, AIConstant.SYLLABUS_KIND_CHAPTER, null);
        BindingResult bindingResult = mock(BindingResult.class);

        // Act + Assert
        assertThatThrownBy(() -> syllabusController.create(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isNull());
        verify(courseRepository, never()).findById(any());
        verify(syllabusRepository, never()).save(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    void shouldCreateChapterSyllabusAndRefreshCourseTotalTimeline() {
        // Arrange
        CreateSyllabusForm form = createForm(1L, AIConstant.SYLLABUS_KIND_CHAPTER, 30);
        BindingResult bindingResult = mock(BindingResult.class);
        Course course = course(1L);
        Syllabus syllabus = new Syllabus();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(syllabusMapper.fromCreateSyllabusFormToEntity(form)).thenReturn(syllabus);
        when(syllabusRepository.sumTimelineByCourseIdAndKind(1L, AIConstant.SYLLABUS_KIND_CHAPTER)).thenReturn(30);

        // Act
        ApiMessageDto<Void> result = syllabusController.create(form, bindingResult);

        // Assert - timeline itself is now mapped by SyllabusMapper (mocked here, so not
        // re-asserted at this unit level - the Controller's job is just the presence check
        // and the totalTimeline rollup, both covered below).
        assertThat(result.getResult()).isTrue();
        verify(syllabusRepository).save(syllabus);
        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(courseCaptor.capture());
        assertThat(courseCaptor.getValue().getTotalTimeline()).isEqualTo(30);
    }

    // ------------------------------------------------------------------ update

    @Test
    void shouldUpdateSyllabusSuccessfully() {
        // Arrange - kind is immutable and not present on UpdateSyllabusForm, so this is
        // a structural check (no kind field to assert), not a runtime kind assertion.
        UpdateSyllabusForm form = updateForm(1L, "avatar.png", 20);
        BindingResult bindingResult = mock(BindingResult.class);
        Course course = course(1L);
        Syllabus syllabus = new Syllabus();
        syllabus.setId(1L);
        syllabus.setKind(AIConstant.SYLLABUS_KIND_LESSON);
        syllabus.setAvatar("avatar.png");
        syllabus.setCourse(course);

        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(syllabus));
        when(syllabusRepository.sumTimelineByCourseIdAndKind(1L, AIConstant.SYLLABUS_KIND_CHAPTER)).thenReturn(20);

        // Act
        ApiMessageDto<Void> result = syllabusController.update(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(syllabusMapper).updateEntityFromForm(form, syllabus);
        verify(syllabusRepository).save(syllabus);
    }

    @Test
    void shouldThrowNotFoundWhenUpdateSyllabusNotFound() {
        // Arrange
        UpdateSyllabusForm form = updateForm(99L, "avatar.png", 20);
        BindingResult bindingResult = mock(BindingResult.class);
        when(syllabusRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> syllabusController.update(form, bindingResult))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.SYLLABUS_ERROR_NOT_FOUND));
        verify(syllabusRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWithoutErrorCodeWhenUpdateChapterSyllabusTimelineToNull() {
        // Arrange - same deliberate no-ErrorCode exception as create's chapter-timeline check.
        UpdateSyllabusForm form = updateForm(1L, "avatar.png", null);
        BindingResult bindingResult = mock(BindingResult.class);
        Course course = course(1L);
        Syllabus syllabus = new Syllabus();
        syllabus.setId(1L);
        syllabus.setKind(AIConstant.SYLLABUS_KIND_CHAPTER);
        syllabus.setAvatar("avatar.png");
        syllabus.setCourse(course);

        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(syllabus));

        // Act + Assert
        assertThatThrownBy(() -> syllabusController.update(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isNull());
        verify(syllabusRepository, never()).save(any());
    }

    @Test
    void shouldDeleteOldAvatarWhenUpdateAvatarChanges() {
        // Arrange
        UpdateSyllabusForm form = updateForm(1L, "/avatar/new.png", 20);
        BindingResult bindingResult = mock(BindingResult.class);
        Course course = course(1L);
        Syllabus syllabus = new Syllabus();
        syllabus.setId(1L);
        syllabus.setKind(AIConstant.SYLLABUS_KIND_LESSON);
        syllabus.setAvatar("/avatar/old.png");
        syllabus.setCourse(course);

        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(syllabus));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(syllabusRepository.sumTimelineByCourseIdAndKind(1L, AIConstant.SYLLABUS_KIND_CHAPTER)).thenReturn(20);

        // Act
        syllabusController.update(form, bindingResult);

        // Assert
        verify(fileService).deleteFile("/avatar/old.png");
    }

    @Test
    void shouldNotDeleteOldAvatarWhenUpdateAvatarUnchanged() {
        // Arrange
        UpdateSyllabusForm form = updateForm(1L, "/avatar/same.png", 20);
        BindingResult bindingResult = mock(BindingResult.class);
        Course course = course(1L);
        Syllabus syllabus = new Syllabus();
        syllabus.setId(1L);
        syllabus.setKind(AIConstant.SYLLABUS_KIND_LESSON);
        syllabus.setAvatar("/avatar/same.png");
        syllabus.setCourse(course);

        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(syllabus));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(syllabusRepository.sumTimelineByCourseIdAndKind(1L, AIConstant.SYLLABUS_KIND_CHAPTER)).thenReturn(20);

        // Act
        syllabusController.update(form, bindingResult);

        // Assert
        verify(fileService, never()).deleteFile(any());
    }

    // --------------------------------------------------------------------- get

    @Test
    void shouldThrowNotFoundWhenGetSyllabusByIdNotFound() {
        // Arrange
        when(syllabusRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> syllabusController.get(99L))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.SYLLABUS_ERROR_NOT_FOUND));
    }

    // -------------------------------------------------------------------- list

    @Test
    @SuppressWarnings("unchecked")
    void shouldListSyllabusesFilteredByCourseId() {
        // Arrange
        SyllabusCriteria criteria = new SyllabusCriteria();
        criteria.setCourseId(1L);
        Pageable pageable = PageRequest.of(0, 10);

        Syllabus syllabus = new Syllabus();
        syllabus.setId(1L);
        Page<Syllabus> page = new PageImpl<>(Collections.singletonList(syllabus), pageable, 1);

        SyllabusDto dto = new SyllabusDto();
        dto.setId(1L);

        when(syllabusRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(syllabusMapper.fromEntityToSyllabusDtoList(anyList())).thenReturn(Collections.singletonList(dto));

        // Act
        ApiMessageDto<ResponseListDto<List<SyllabusDto>>> result = syllabusController.list(criteria, pageable);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).hasSize(1);
        verify(syllabusRepository).findAll(any(Specification.class), eq(pageable));
    }

    // ------------------------------------------------------------------ delete

    @Test
    void shouldThrowNotFoundWhenDeleteSyllabusNotFound() {
        // Arrange
        when(syllabusRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> syllabusController.delete(99L))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.SYLLABUS_ERROR_NOT_FOUND));
        verify(syllabusRepository, never()).deleteById(any());
        verify(fileService, never()).deleteFile(any());
    }

    @Test
    void shouldDeleteSyllabusAndRefreshCourseTotalTimeline() {
        // Arrange
        Course course = course(1L);
        Syllabus syllabus = new Syllabus();
        syllabus.setId(1L);
        syllabus.setCourse(course);
        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(syllabus));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(syllabusRepository.sumTimelineByCourseIdAndKind(1L, AIConstant.SYLLABUS_KIND_CHAPTER)).thenReturn(15);

        // Act
        ApiMessageDto<Void> result = syllabusController.delete(1L);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(syllabusRepository).deleteById(1L);
        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(courseCaptor.capture());
        assertThat(courseCaptor.getValue().getTotalTimeline()).isEqualTo(15);
    }

    @Test
    void shouldDeleteAvatarFileWhenDeletingSyllabusWithNonBlankAvatar() {
        // Arrange
        Course course = course(1L);
        Syllabus syllabus = new Syllabus();
        syllabus.setId(1L);
        syllabus.setAvatar("/avatar/to-delete.png");
        syllabus.setCourse(course);
        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(syllabus));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(syllabusRepository.sumTimelineByCourseIdAndKind(1L, AIConstant.SYLLABUS_KIND_CHAPTER)).thenReturn(0);

        // Act
        syllabusController.delete(1L);

        // Assert
        verify(fileService).deleteFile("/avatar/to-delete.png");
    }

    @Test
    void shouldNotDeleteFileWhenDeletingSyllabusWithBlankAvatar() {
        // Arrange
        Course course = course(1L);
        Syllabus syllabus = new Syllabus();
        syllabus.setId(1L);
        syllabus.setCourse(course);
        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(syllabus));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(syllabusRepository.sumTimelineByCourseIdAndKind(1L, AIConstant.SYLLABUS_KIND_CHAPTER)).thenReturn(0);

        // Act
        syllabusController.delete(1L);

        // Assert
        verify(fileService, never()).deleteFile(any());
    }

    // ------------------------------------------------------------ update-ordering

    @Test
    void shouldUpdateOrderingInSingleSaveAllBatchCall() {
        // Arrange
        UpdateSyllabusOrderingForm form1 = new UpdateSyllabusOrderingForm();
        form1.setId(1L);
        form1.setOrdering(10);
        UpdateSyllabusOrderingForm form2 = new UpdateSyllabusOrderingForm();
        form2.setId(2L);
        form2.setOrdering(20);

        Syllabus syllabus1 = new Syllabus();
        syllabus1.setId(1L);
        Syllabus syllabus2 = new Syllabus();
        syllabus2.setId(2L);

        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(syllabus1));
        when(syllabusRepository.findById(2L)).thenReturn(Optional.of(syllabus2));

        // Act
        syllabusController.updateOrdering(Arrays.asList(form1, form2));

        // Assert
        assertThat(syllabus1.getOrdering()).isEqualTo(10);
        assertThat(syllabus2.getOrdering()).isEqualTo(20);
        verify(syllabusRepository, never()).save(any());
        ArgumentCaptor<List<Syllabus>> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(syllabusRepository, times(1)).saveAll(listCaptor.capture());
        assertThat(listCaptor.getValue()).containsExactlyInAnyOrder(syllabus1, syllabus2);
    }

    // --------------------------------------------------------------- publicList

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnPublicListScopedToCourseIdAndActiveStatus() {
        // Arrange
        SyllabusCriteria criteria = new SyllabusCriteria();
        criteria.setCourseId(1L);
        criteria.setStatus(0); // requested status other than ACTIVE - must be overridden
        Pageable pageable = PageRequest.of(0, 10);

        Syllabus syllabus = new Syllabus();
        syllabus.setId(1L);
        Page<Syllabus> page = new PageImpl<>(Collections.singletonList(syllabus), pageable, 1);

        SyllabusDto dto = new SyllabusDto();
        dto.setId(1L);

        when(syllabusRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(syllabusMapper.fromEntityToSyllabusDtoList(anyList())).thenReturn(Collections.singletonList(dto));

        // Act
        ApiMessageDto<ResponseListDto<List<SyllabusDto>>> result = syllabusController.publicList(criteria, pageable);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).hasSize(1);
        assertThat(criteria.getStatus()).isEqualTo(AIConstant.STATUS_ACTIVE);
        verify(syllabusRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void shouldThrowBadRequestWithoutErrorCodeWhenPublicListMissingCourseId() {
        // Arrange - deliberate exception to the usual ErrorCode rule: message-only constructor,
        // getCode() MUST be null for this one case. Do not "fix" this assertion.
        SyllabusCriteria criteria = new SyllabusCriteria();
        Pageable pageable = PageRequest.of(0, 10);

        // Act + Assert
        assertThatThrownBy(() -> syllabusController.publicList(criteria, pageable))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isNull());
        verify(syllabusRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }
}
