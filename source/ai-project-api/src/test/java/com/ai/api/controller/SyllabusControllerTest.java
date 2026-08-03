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
 * reversed chapter/lesson timeline invariant (chapter timeline defaults to 0 and is never
 * client-supplied; lesson timeline is required and its chapter timeline is adjusted by delta,
 * since Syllabus has no persisted chapter association - chapterId is a per-request hint only),
 * course total-timeline recomputation, avatar-file cleanup on update/delete, the update-ordering
 * batch endpoint (full per-batch chapter recompute), and the unauthenticated publicList endpoint.
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

    private UpdateSyllabusForm updateForm(Long id, String avatar, Integer timeline, Long chapterId) {
        UpdateSyllabusForm form = new UpdateSyllabusForm();
        form.setId(id);
        form.setName("Intro updated");
        form.setAvatar(avatar);
        form.setDescription("description updated");
        form.setTimeline(timeline);
        form.setChapterId(chapterId);
        return form;
    }

    private Course course(Long id) {
        Course course = new Course();
        course.setId(id);
        course.setAvatar("course-avatar.png");
        return course;
    }

    private Syllabus chapterSyllabus(Long id, Course course, Integer timeline) {
        Syllabus chapter = new Syllabus();
        chapter.setId(id);
        chapter.setKind(AIConstant.SYLLABUS_KIND_CHAPTER);
        chapter.setCourse(course);
        chapter.setTimeline(timeline);
        return chapter;
    }

    private Syllabus lessonSyllabus(Long id, Course course, Integer timeline) {
        Syllabus lesson = new Syllabus();
        lesson.setId(id);
        lesson.setKind(AIConstant.SYLLABUS_KIND_LESSON);
        lesson.setCourse(course);
        lesson.setTimeline(timeline);
        return lesson;
    }

    // ------------------------------------------------------------------ create

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
    void shouldCreateChapterSyllabusAlwaysStartsAtZeroTimeline() {
        // Arrange - reversed invariant: a chapter's own timeline is never client-supplied. The
        // entity itself defaults `timeline` to 0 (field initializer) - the Controller no longer
        // has any CHAPTER-specific branch at all.
        CreateSyllabusForm form = createForm(1L, AIConstant.SYLLABUS_KIND_CHAPTER, null);
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
        assertThat(syllabus.getTimeline()).isEqualTo(0);
        assertThat(syllabus.getCourse()).isEqualTo(course);
        verify(syllabusRepository).save(syllabus);
        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(courseCaptor.capture());
        assertThat(courseCaptor.getValue().getTotalTimeline()).isEqualTo(0);
    }

    @Test
    void shouldCreateLessonSyllabusRequiresTimelineAndAutoAssignsHighestOrderingChapter() {
        // Arrange - lesson now carries the required timeline and is added to the highest-
        // ordering chapter of its course; that chapter's timeline is adjusted by the lesson's
        // timeline (delta add - the chapter row itself is mutated directly, no FK to persist).
        CreateSyllabusForm form = createForm(1L, AIConstant.SYLLABUS_KIND_LESSON, 45);
        BindingResult bindingResult = mock(BindingResult.class);
        Course course = course(1L);
        Syllabus chapter = chapterSyllabus(5L, course, 0);
        Syllabus syllabus = new Syllabus();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(syllabusMapper.fromCreateSyllabusFormToEntity(form)).thenReturn(syllabus);
        when(syllabusRepository.findTopByCourseIdAndKindOrderByOrderingDesc(1L, AIConstant.SYLLABUS_KIND_CHAPTER))
                .thenReturn(Optional.of(chapter));
        when(syllabusRepository.sumTimelineByCourseIdAndKind(1L, AIConstant.SYLLABUS_KIND_CHAPTER)).thenReturn(45);

        // Act
        ApiMessageDto<Void> result = syllabusController.create(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(syllabus.getTimeline()).isEqualTo(45);
        assertThat(chapter.getTimeline()).isEqualTo(45);
        // Entity equals()/hashCode() (inherited from the ReuseId base) only compares a
        // never-set `reusedId` field, so every entity instance is "equal" to every other -
        // verify by call count and rely on the direct state assertions above instead of
        // verify(mock).save(specificInstance), which Mockito cannot disambiguate here.
        verify(syllabusRepository, times(2)).save(any());

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(courseCaptor.capture());
        assertThat(courseCaptor.getValue().getTotalTimeline()).isEqualTo(45);
    }

    @Test
    void shouldThrowBadRequestWhenCreateLessonSyllabusWithoutTimeline() {
        // Arrange - deliberate exception to the usual ErrorCode rule: message-only constructor,
        // getCode() MUST be null for this one case, consistent with the file's established
        // pattern for business-rule validation checks.
        CreateSyllabusForm form = createForm(1L, AIConstant.SYLLABUS_KIND_LESSON, null);
        BindingResult bindingResult = mock(BindingResult.class);
        Course course = course(1L);
        Syllabus syllabus = new Syllabus();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(syllabusMapper.fromCreateSyllabusFormToEntity(form)).thenReturn(syllabus);

        // Act + Assert
        assertThatThrownBy(() -> syllabusController.create(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isNull());
        verify(syllabusRepository, never()).save(any());
        verify(courseRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenCreateLessonSyllabusWithNoChapterInCourse() {
        // Arrange
        CreateSyllabusForm form = createForm(1L, AIConstant.SYLLABUS_KIND_LESSON, 30);
        BindingResult bindingResult = mock(BindingResult.class);
        Course course = course(1L);
        Syllabus syllabus = new Syllabus();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(syllabusMapper.fromCreateSyllabusFormToEntity(form)).thenReturn(syllabus);
        when(syllabusRepository.findTopByCourseIdAndKindOrderByOrderingDesc(1L, AIConstant.SYLLABUS_KIND_CHAPTER))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> syllabusController.create(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isNull());
        verify(syllabusRepository, never()).save(any());
        verify(courseRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ update

    @Test
    void shouldUpdateLessonSyllabusSuccessfullyWithChapterId() {
        // Arrange - lesson update requires a valid chapterId; the referenced chapter's timeline
        // is adjusted by the delta between the old and new lesson timeline.
        UpdateSyllabusForm form = updateForm(1L, "avatar.png", 30, 5L);
        BindingResult bindingResult = mock(BindingResult.class);
        Course course = course(1L);
        Syllabus chapter = chapterSyllabus(5L, course, 20);
        Syllabus syllabus = lessonSyllabus(1L, course, 20);
        syllabus.setAvatar("avatar.png");

        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(syllabus));
        when(syllabusRepository.findById(5L)).thenReturn(Optional.of(chapter));
        when(syllabusRepository.sumTimelineByCourseIdAndKind(1L, AIConstant.SYLLABUS_KIND_CHAPTER)).thenReturn(30);

        // Act
        ApiMessageDto<Void> result = syllabusController.update(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(syllabusMapper).updateEntityFromForm(form, syllabus);
        assertThat(chapter.getTimeline()).isEqualTo(30);
        // See the note in the create-lesson test above: entity equals() is unusable for
        // per-instance verify(), so assert by count + captured state instead.
        verify(syllabusRepository, times(2)).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenUpdateSyllabusNotFound() {
        // Arrange
        UpdateSyllabusForm form = updateForm(99L, "avatar.png", 20, 5L);
        BindingResult bindingResult = mock(BindingResult.class);
        when(syllabusRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> syllabusController.update(form, bindingResult))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.SYLLABUS_ERROR_NOT_FOUND));
        verify(syllabusRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenUpdateLessonSyllabusWithoutChapterId() {
        // Arrange
        UpdateSyllabusForm form = updateForm(1L, "avatar.png", 20, null);
        BindingResult bindingResult = mock(BindingResult.class);
        Course course = course(1L);
        Syllabus syllabus = lessonSyllabus(1L, course, 20);
        syllabus.setAvatar("avatar.png");

        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(syllabus));

        // Act + Assert
        assertThatThrownBy(() -> syllabusController.update(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isNull());
        verify(syllabusRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenUpdateLessonSyllabusWithNonChapterId() {
        // Arrange - chapterId points to another lesson, not a chapter.
        UpdateSyllabusForm form = updateForm(1L, "avatar.png", 20, 9L);
        BindingResult bindingResult = mock(BindingResult.class);
        Course course = course(1L);
        Syllabus syllabus = lessonSyllabus(1L, course, 20);
        syllabus.setAvatar("avatar.png");
        Syllabus notAChapter = lessonSyllabus(9L, course, 10);

        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(syllabus));
        when(syllabusRepository.findById(9L)).thenReturn(Optional.of(notAChapter));

        // Act + Assert
        assertThatThrownBy(() -> syllabusController.update(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isNull());
        verify(syllabusRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenUpdateLessonSyllabusWithChapterIdFromOtherCourse() {
        // Arrange - chapterId points to a real chapter, but of a different course.
        UpdateSyllabusForm form = updateForm(1L, "avatar.png", 20, 7L);
        BindingResult bindingResult = mock(BindingResult.class);
        Course course = course(1L);
        Course otherCourse = course(2L);
        Syllabus syllabus = lessonSyllabus(1L, course, 20);
        syllabus.setAvatar("avatar.png");
        Syllabus chapterOfOtherCourse = chapterSyllabus(7L, otherCourse, 0);

        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(syllabus));
        when(syllabusRepository.findById(7L)).thenReturn(Optional.of(chapterOfOtherCourse));

        // Act + Assert
        assertThatThrownBy(() -> syllabusController.update(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isNull());
        verify(syllabusRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenUpdateLessonSyllabusWithUnknownChapterId() {
        // Arrange - chapterId does not resolve to any Syllabus row at all.
        UpdateSyllabusForm form = updateForm(1L, "avatar.png", 20, 999L);
        BindingResult bindingResult = mock(BindingResult.class);
        Course course = course(1L);
        Syllabus syllabus = lessonSyllabus(1L, course, 20);
        syllabus.setAvatar("avatar.png");

        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(syllabus));
        when(syllabusRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> syllabusController.update(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isNull());
        verify(syllabusRepository, never()).save(any());
    }

    @Test
    void shouldDeleteOldAvatarWhenUpdateAvatarChanges() {
        // Arrange - the Controller compares the entity's current avatar against the form's new
        // avatar BEFORE calling the mapper, so no separate oldAvatar variable/mapper stubbing
        // is needed here.
        UpdateSyllabusForm form = updateForm(1L, "/avatar/new.png", 20, 5L);
        BindingResult bindingResult = mock(BindingResult.class);
        Course course = course(1L);
        Syllabus chapter = chapterSyllabus(5L, course, 20);
        Syllabus syllabus = lessonSyllabus(1L, course, 20);
        syllabus.setAvatar("/avatar/old.png");

        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(syllabus));
        when(syllabusRepository.findById(5L)).thenReturn(Optional.of(chapter));
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
        UpdateSyllabusForm form = updateForm(1L, "/avatar/same.png", 20, 5L);
        BindingResult bindingResult = mock(BindingResult.class);
        Course course = course(1L);
        Syllabus chapter = chapterSyllabus(5L, course, 20);
        Syllabus syllabus = lessonSyllabus(1L, course, 20);
        syllabus.setAvatar("/avatar/same.png");

        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(syllabus));
        when(syllabusRepository.findById(5L)).thenReturn(Optional.of(chapter));
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
        assertThatThrownBy(() -> syllabusController.delete(99L, null))
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
        ApiMessageDto<Void> result = syllabusController.delete(1L, null);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(syllabusRepository).deleteById(1L);
        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(courseCaptor.capture());
        assertThat(courseCaptor.getValue().getTotalTimeline()).isEqualTo(15);
    }

    @Test
    void shouldRecomputeChapterTimelineWhenDeletingLessonWithChapterIdParam() {
        // Arrange - deleting a lesson with a chapterId query param subtracts its timeline from
        // that chapter, then the course total is recomputed.
        Course course = course(1L);
        Syllabus chapter = chapterSyllabus(5L, course, 20);
        Syllabus syllabus = lessonSyllabus(1L, course, 20);

        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(syllabus));
        when(syllabusRepository.findById(5L)).thenReturn(Optional.of(chapter));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(syllabusRepository.sumTimelineByCourseIdAndKind(1L, AIConstant.SYLLABUS_KIND_CHAPTER)).thenReturn(0);

        // Act
        syllabusController.delete(1L, 5L);

        // Assert
        verify(syllabusRepository).deleteById(1L);
        assertThat(chapter.getTimeline()).isEqualTo(0);
        verify(syllabusRepository).save(chapter);

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(courseCaptor.capture());
        assertThat(courseCaptor.getValue().getTotalTimeline()).isEqualTo(0);
    }

    @Test
    void shouldThrowBadRequestWhenDeletingLessonWithoutChapterIdParam() {
        // Arrange - chapterId is required whenever the row being deleted is a lesson; nothing
        // is deleted or adjusted if it's missing.
        Course course = course(1L);
        Syllabus syllabus = lessonSyllabus(1L, course, 20);
        syllabus.setAvatar("/avatar/lesson.png");

        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(syllabus));

        // Act + Assert
        assertThatThrownBy(() -> syllabusController.delete(1L, null))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isNull());
        verify(fileService, never()).deleteFile(any());
        verify(syllabusRepository, never()).deleteById(any());
        verify(syllabusRepository, never()).save(any());
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
        syllabusController.delete(1L, null);

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
        syllabusController.delete(1L, null);

        // Assert
        verify(fileService, never()).deleteFile(any());
    }

    // ------------------------------------------------------------ update-ordering

    @Test
    void shouldUpdateOrderingInSingleSaveAllBatchCall() {
        // Arrange - no chapterId on any item: ordering only, no chapter/course timeline touched.
        UpdateSyllabusOrderingForm form1 = new UpdateSyllabusOrderingForm();
        form1.setId(1L);
        form1.setOrdering(10);
        UpdateSyllabusOrderingForm form2 = new UpdateSyllabusOrderingForm();
        form2.setId(2L);
        form2.setOrdering(20);

        Course course = course(1L);
        Syllabus syllabus1 = new Syllabus();
        syllabus1.setId(1L);
        syllabus1.setCourse(course);
        Syllabus syllabus2 = new Syllabus();
        syllabus2.setId(2L);
        syllabus2.setCourse(course);

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

    @Test
    void shouldRecomputeChapterTimelineFromBatchWhenUpdateOrderingSuppliesChapterId() {
        // Arrange - every lesson item in the batch carries its chapterId; the chapter's timeline
        // is recomputed as the sum of every lesson item in THIS batch pointing to it (a full
        // recompute, not a delta - there is no persisted "previous chapter" to diff against).
        UpdateSyllabusOrderingForm lessonForm1 = new UpdateSyllabusOrderingForm();
        lessonForm1.setId(1L);
        lessonForm1.setOrdering(10);
        lessonForm1.setChapterId(6L);
        UpdateSyllabusOrderingForm lessonForm2 = new UpdateSyllabusOrderingForm();
        lessonForm2.setId(2L);
        lessonForm2.setOrdering(11);
        lessonForm2.setChapterId(6L);

        Course course = course(1L);
        Syllabus chapter = chapterSyllabus(6L, course, 0);
        Syllabus lesson1 = lessonSyllabus(1L, course, 15);
        Syllabus lesson2 = lessonSyllabus(2L, course, 20);

        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(lesson1));
        when(syllabusRepository.findById(2L)).thenReturn(Optional.of(lesson2));
        when(syllabusRepository.findById(6L)).thenReturn(Optional.of(chapter));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(syllabusRepository.sumTimelineByCourseIdAndKind(1L, AIConstant.SYLLABUS_KIND_CHAPTER)).thenReturn(35);

        // Act
        syllabusController.updateOrdering(Arrays.asList(lessonForm1, lessonForm2));

        // Assert
        assertThat(lesson1.getOrdering()).isEqualTo(10);
        assertThat(lesson2.getOrdering()).isEqualTo(11);
        assertThat(chapter.getTimeline()).isEqualTo(35);
        verify(syllabusRepository).save(chapter);

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository, times(1)).save(courseCaptor.capture());
        assertThat(courseCaptor.getValue().getTotalTimeline()).isEqualTo(35);
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
