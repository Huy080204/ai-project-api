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
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
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
        if (AIConstant.SYLLABUS_KIND_CHAPTER.equals(createSyllabusForm.getKind())) {
            if (createSyllabusForm.getTimeline() == null) {
                throw new BadRequestException("Timeline is required for chapter");
            }
            syllabus.setTimeline(createSyllabusForm.getTimeline());
        }
        syllabus.setCourse(course);
        syllabusRepository.save(syllabus);
        refreshCourseTotalTimeline(createSyllabusForm.getCourseId());
        return makeSuccessResponse("Create syllabus success");
    }

    @Transactional
    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SYL_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateSyllabusForm updateSyllabusForm, BindingResult bindingResult) {
        Syllabus syllabus = syllabusRepository.findById(updateSyllabusForm.getId())
                .orElseThrow(() -> new NotFoundException("Syllabus not found", ErrorCode.SYLLABUS_ERROR_NOT_FOUND));
        if (AIConstant.SYLLABUS_KIND_CHAPTER.equals(syllabus.getKind())) {
            if (updateSyllabusForm.getTimeline() == null) {
                throw new BadRequestException("Timeline is required for chapter");
            }
            syllabus.setTimeline(updateSyllabusForm.getTimeline());
        }
        if (StringUtils.isNoneBlank(syllabus.getAvatar()) && !Objects.equals(updateSyllabusForm.getAvatar(), syllabus.getAvatar())) {
            fileService.deleteFile(syllabus.getAvatar());
        }
        syllabusMapper.updateEntityFromForm(updateSyllabusForm, syllabus);
        syllabusRepository.save(syllabus);
        refreshCourseTotalTimeline(syllabus.getCourse().getId());
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
    public ApiMessageDto<ResponseListDto<List<SyllabusDto>>> list(SyllabusCriteria criteria, Pageable pageable) {
        Page<Syllabus> syllabuses = syllabusRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(syllabuses, syllabusMapper::fromEntityToSyllabusDtoList), "List syllabus success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SYL_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        Syllabus syllabus = syllabusRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Syllabus not found", ErrorCode.SYLLABUS_ERROR_NOT_FOUND));
        if (StringUtils.isNoneBlank(syllabus.getAvatar())) {
            fileService.deleteFile(syllabus.getAvatar());
        }
        Long courseId = syllabus.getCourse().getId();
        syllabusRepository.deleteById(id);
        refreshCourseTotalTimeline(courseId);
        return makeSuccessResponse("Delete syllabus success");
    }

    @Transactional
    @PutMapping(value = "/update-ordering", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SYL_U')")
    public ApiMessageDto<Void> updateOrdering(@Valid @RequestBody List<UpdateSyllabusOrderingForm> updateSyllabusOrderingForms) {
        List<Syllabus> syllabuses = new ArrayList<>();
        for (UpdateSyllabusOrderingForm form : updateSyllabusOrderingForms) {
            Syllabus syllabus = syllabusRepository.findById(form.getId())
                    .orElseThrow(() -> new NotFoundException("Syllabus not found", ErrorCode.SYLLABUS_ERROR_NOT_FOUND));
            syllabus.setOrdering(form.getOrdering());
            syllabuses.add(syllabus);
        }
        syllabusRepository.saveAll(syllabuses);
        return makeSuccessResponse("Update syllabus ordering success");
    }

    @GetMapping(value = "/public/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<SyllabusDto>>> publicList(SyllabusCriteria criteria, Pageable pageable) {
        if (criteria.getCourseId() == null) {
            throw new BadRequestException("courseId is required");
        }
        criteria.setStatus(AIConstant.STATUS_ACTIVE);
        Page<Syllabus> syllabuses = syllabusRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(syllabuses, syllabusMapper::fromEntityToSyllabusDtoList), "List syllabus success");
    }

    private void refreshCourseTotalTimeline(Long courseId) {
        Integer sum = syllabusRepository.sumTimelineByCourseIdAndKind(courseId, AIConstant.SYLLABUS_KIND_CHAPTER);
        courseRepository.findById(courseId).ifPresent(course -> {
            course.setTotalTimeline(sum);
            courseRepository.save(course);
        });
    }
}
