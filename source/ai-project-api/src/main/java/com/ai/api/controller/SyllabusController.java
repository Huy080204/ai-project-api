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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/v1/syllabus")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class SyllabusController extends ABasicController {
    @Autowired
    private SyllabusRepository syllabusRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SyllabusMapper syllabusMapper;

    @Autowired
    private FileService fileService;

    @Transactional
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SYL_C')")
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateSyllabusForm createSyllabusForm, BindingResult bindingResult) {
        Course course = courseRepository.findById(createSyllabusForm.getCourseId())
                .orElseThrow(() -> new NotFoundException("Course not found", ErrorCode.COURSE_ERROR_NOT_FOUND));
        Syllabus syllabus = syllabusMapper.fromCreateSyllabusFormToEntity(createSyllabusForm);
        syllabus.setCourse(course);

        if (AIConstant.SYLLABUS_KIND_LESSON.equals(createSyllabusForm.getKind())) {
            if (createSyllabusForm.getTimeline() == null) {
                throw new BadRequestException("Timeline is required for lesson");
            }
            Syllabus chapter = syllabusRepository.findTopByCourseIdAndKindOrderByOrderingDesc(
                            createSyllabusForm.getCourseId(), AIConstant.SYLLABUS_KIND_CHAPTER)
                    .orElseThrow(() -> new BadRequestException(
                            "Course must have at least one chapter before adding a lesson"));
            syllabus.setTimeline(createSyllabusForm.getTimeline());
            chapter.setTimeline(chapter.getTimeline() + syllabus.getTimeline());
            syllabusRepository.save(chapter);

            syllabusRepository.save(syllabus);
            courseRepository.updateTotalTimelineByDelta(createSyllabusForm.getCourseId(), syllabus.getTimeline());
        } else {
            syllabusRepository.save(syllabus);
        }
        return makeSuccessResponse("Create syllabus success");
    }

    @Transactional
    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SYL_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateSyllabusForm updateSyllabusForm, BindingResult bindingResult) {
        Syllabus syllabus = syllabusRepository.findById(updateSyllabusForm.getId())
                .orElseThrow(() -> new NotFoundException("Syllabus not found", ErrorCode.SYLLABUS_ERROR_NOT_FOUND));

        if (AIConstant.SYLLABUS_KIND_LESSON.equals(syllabus.getKind())) {
            if (updateSyllabusForm.getTimeline() == null) {
                throw new BadRequestException("Timeline is required for lesson");
            }
            if (updateSyllabusForm.getChapterId() == null) {
                throw new BadRequestException("chapterId is required for lesson");
            }
            Syllabus chapter = resolveChapter(updateSyllabusForm.getChapterId(), syllabus.getCourse().getId());
            Integer timelineDelta = updateSyllabusForm.getTimeline() - syllabus.getTimeline();
            syllabus.setTimeline(updateSyllabusForm.getTimeline());
            chapter.setTimeline(chapter.getTimeline() + timelineDelta);
            syllabusRepository.save(chapter);
            courseRepository.updateTotalTimelineByDelta(syllabus.getCourse().getId(), timelineDelta);
        }

        if (StringUtils.isNoneBlank(syllabus.getAvatar())
                && !Objects.equals(updateSyllabusForm.getAvatar(), syllabus.getAvatar())) {
            fileService.deleteFile(syllabus.getAvatar());
        }
        syllabusMapper.updateEntityFromForm(updateSyllabusForm, syllabus);
        syllabusRepository.save(syllabus);
        return makeSuccessResponse("Update syllabus success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SYL_V')")
    public ApiMessageDto<SyllabusDto> get(@PathVariable("id") Long id) {
        Syllabus syllabus = syllabusRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Syllabus not found", ErrorCode.SYLLABUS_ERROR_NOT_FOUND));
        return makeSuccessResponse(syllabusMapper.fromEntityToSyllabusDto(syllabus), "Get syllabus success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SYL_L')")
    public ApiMessageDto<ResponseListDto<List<SyllabusDto>>> list(SyllabusCriteria criteria, @PageableDefault(sort = "ordering", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Syllabus> syllabuses = syllabusRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(syllabuses, syllabusMapper::fromEntityToSyllabusDtoList), "List syllabus success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SYL_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id,
                                      @RequestParam(value = "chapterId", required = false) Long chapterId) {
        Syllabus syllabus = syllabusRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Syllabus not found", ErrorCode.SYLLABUS_ERROR_NOT_FOUND));
        Long courseId = syllabus.getCourse().getId();

        if (AIConstant.SYLLABUS_KIND_LESSON.equals(syllabus.getKind())) {
            // Handle lesson deletion: update parent chapter timeline and subtract from course total timeline
            if (chapterId == null) {
                throw new BadRequestException("chapterId is required for lesson");
            }
            Syllabus chapter = resolveChapter(chapterId, courseId);
            chapter.setTimeline(chapter.getTimeline() - syllabus.getTimeline());
            syllabusRepository.save(chapter);

            syllabusRepository.deleteById(id);
            courseRepository.updateTotalTimelineByDelta(courseId, -syllabus.getTimeline());
        } else if (AIConstant.SYLLABUS_KIND_CHAPTER.equals(syllabus.getKind())) {
            // Handle chapter deletion: merge timeline into upper chapter or prevent orphaned lessons
            Syllabus upperChapter = syllabusRepository.findTopByCourseIdAndKindAndOrderingLessThanOrderByOrderingDesc(
                    courseId, AIConstant.SYLLABUS_KIND_CHAPTER, syllabus.getOrdering()).orElse(null);
            if (upperChapter != null) {
                upperChapter.setTimeline(upperChapter.getTimeline() + syllabus.getTimeline());
                syllabusRepository.save(upperChapter);
            } else {
                // Prevent deleting the first chapter if it leaves orphaned lessons below it
                Syllabus below = syllabusRepository.findTopByCourseIdAndOrderingGreaterThanOrderByOrderingAsc(
                        courseId, syllabus.getOrdering()).orElse(null);
                if (below != null && AIConstant.SYLLABUS_KIND_LESSON.equals(below.getKind())) {
                    throw new BadRequestException("Cannot delete the first chapter while a lesson remains under it", ErrorCode.SYLLABUS_ERROR_UNABLE_DELETE);
                }
            }

            syllabusRepository.deleteById(id);
        }

        if (StringUtils.isNoneBlank(syllabus.getAvatar())) {
            fileService.deleteFile(syllabus.getAvatar());
        }
        return makeSuccessResponse("Delete syllabus success");
    }

    @Transactional
    @PutMapping(value = "/update-ordering", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SYL_U')")
    public ApiMessageDto<Void> updateOrdering(@Valid @RequestBody List<UpdateSyllabusOrderingForm> updateSyllabusOrderingForms) {
        List<Syllabus> syllabuses = new ArrayList<>();
        Map<Long, Integer> chapterTimelineSums = new HashMap<>();
        Long courseId = null;

        for (UpdateSyllabusOrderingForm form : updateSyllabusOrderingForms) {
            Syllabus syllabus = syllabusRepository.findById(form.getId())
                    .orElseThrow(() -> new NotFoundException("Syllabus not found", ErrorCode.SYLLABUS_ERROR_NOT_FOUND));
            syllabus.setOrdering(form.getOrdering());
            courseId = syllabus.getCourse().getId();

            if (AIConstant.SYLLABUS_KIND_CHAPTER.equals(syllabus.getKind())) {
                chapterTimelineSums.putIfAbsent(syllabus.getId(), 0);
            } else if (AIConstant.SYLLABUS_KIND_LESSON.equals(syllabus.getKind())) {
                if (form.getChapterId() == null) {
                    throw new BadRequestException("chapterId is required for lesson");
                }
                Integer timeline = syllabus.getTimeline() == null ? 0 : syllabus.getTimeline();
                chapterTimelineSums.merge(form.getChapterId(), timeline, Integer::sum);
            }
            syllabuses.add(syllabus);
        }
        syllabusRepository.saveAll(syllabuses);

        for (Map.Entry<Long, Integer> entry : chapterTimelineSums.entrySet()) {
            Syllabus chapter = resolveChapter(entry.getKey(), courseId);
            chapter.setTimeline(entry.getValue());
            syllabusRepository.save(chapter);
        }
        return makeSuccessResponse("Update syllabus ordering success");
    }

    @GetMapping(value = "/public/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<SyllabusDto>>> publicList(SyllabusCriteria criteria, @PageableDefault(sort = "ordering", direction = Sort.Direction.ASC) Pageable pageable) {
        if (criteria.getCourseId() == null) {
            throw new BadRequestException("courseId is required");
        }
        criteria.setStatus(AIConstant.STATUS_ACTIVE);
        Page<Syllabus> syllabuses = syllabusRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(syllabuses, syllabusMapper::fromEntityToSyllabusDtoList), "List syllabus success");
    }

    private Syllabus resolveChapter(Long chapterId, Long courseId) {
        Syllabus chapter = syllabusRepository.findById(chapterId)
                .orElseThrow(() -> new BadRequestException("chapterId does not reference an existing Chapter"));
        if (!AIConstant.SYLLABUS_KIND_CHAPTER.equals(chapter.getKind())
                || chapter.getCourse() == null || !Objects.equals(chapter.getCourse().getId(), courseId)) {
            throw new BadRequestException("chapterId must reference a Chapter in the same course");
        }
        return chapter;
    }
}
