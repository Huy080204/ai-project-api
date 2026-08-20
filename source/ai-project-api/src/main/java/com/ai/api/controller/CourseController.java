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
import com.ai.api.repository.AssignmentRepository;
import com.ai.api.repository.ClassroomRepository;
import com.ai.api.repository.ClassroomStudentRepository;
import com.ai.api.repository.CourseRepository;
import com.ai.api.repository.RatingRepository;
import com.ai.api.repository.ReactionRepository;
import com.ai.api.repository.RegistrationRepository;
import com.ai.api.repository.SubmissionRepository;
import com.ai.api.repository.SyllabusMaterialRepository;
import com.ai.api.repository.SyllabusRepository;
import com.ai.api.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
@RequestMapping("/v1/course")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class CourseController extends ABasicController {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private SyllabusRepository syllabusRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private ClassroomStudentRepository classroomStudentRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private SyllabusMaterialRepository syllabusMaterialRepository;

    @Transactional
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COU_C')")
    public ApiMessageDto<CourseDto> create(@Valid @RequestBody CreateCourseForm createCourseForm, BindingResult bindingResult) {
        if (courseRepository.existsByName(createCourseForm.getName())) {
            throw new BadRequestException("Course name already exist", ErrorCode.COURSE_ERROR_NAME_EXIST);
        }
        Course course = courseMapper.fromCreateCourseFormToEntity(createCourseForm);
        courseRepository.save(course);
        return makeSuccessResponse(courseMapper.fromEntityToCourseIdDto(course), "Create course success");
    }

    @Transactional
    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COU_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateCourseForm updateCourseForm, BindingResult bindingResult) {
        Course course = courseRepository.findById(updateCourseForm.getId())
                .orElseThrow(() -> new NotFoundException("Course not found", ErrorCode.COURSE_ERROR_NOT_FOUND));
        if (courseRepository.existsByNameAndIdNot(updateCourseForm.getName(), updateCourseForm.getId())) {
            throw new BadRequestException("Course name already exist", ErrorCode.COURSE_ERROR_NAME_EXIST);
        }
        String oldAvatar = course.getAvatar();
        if (StringUtils.isNoneBlank(oldAvatar) && !Objects.equals(updateCourseForm.getAvatar(), oldAvatar)) {
            fileService.deleteFile(oldAvatar);
        }
        courseMapper.updateEntityFromForm(updateCourseForm, course);
        courseRepository.save(course);
        return makeSuccessResponse("Update course success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COU_V')")
    public ApiMessageDto<CourseDto> get(@PathVariable("id") Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course not found", ErrorCode.COURSE_ERROR_NOT_FOUND));
        return makeSuccessResponse(courseMapper.fromEntityToCourseDto(course), "Get course success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COU_L')")
    public ApiMessageDto<ResponseListDto<List<CourseDto>>> list(CourseCriteria criteria, Pageable pageable) {
        Page<Course> courses = courseRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(courses, courseMapper::fromEntityToCourseDtoList), "List course success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('COU_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course not found", ErrorCode.COURSE_ERROR_NOT_FOUND));
        List<String> filesToDelete = new ArrayList<>();
        if (StringUtils.isNoneBlank(course.getAvatar())) {
            filesToDelete.add(course.getAvatar());
        }
        filesToDelete.addAll(syllabusRepository.findAvatarsByCourseId(id));
        filesToDelete.addAll(assignmentRepository.findFileAttachmentUrlsBySyllabusCourseId(id));
        filesToDelete.addAll(submissionRepository.findFileUrlsBySyllabusCourseId(id));
        filesToDelete.addAll(syllabusMaterialRepository.findFileUrlsBySyllabusCourseId(id));
        fileService.deleteFiles(filesToDelete);
        registrationRepository.deleteAllByClassroomCourseId(id);
        classroomStudentRepository.deleteAllByClassroomCourseId(id);
        classroomRepository.deleteAllByCourseId(id);
        submissionRepository.deleteAllBySyllabusCourseId(id);
        assignmentRepository.deleteAllBySyllabusCourseId(id);
        syllabusMaterialRepository.deleteAllBySyllabusCourseId(id);
        syllabusRepository.deleteAllByCourseId(id);
        ratingRepository.deleteAllByCourseId(id);
        reactionRepository.deleteAllByCourseId(id);
        courseRepository.deleteById(id);
        return makeSuccessResponse("Delete course success");
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<CourseDto>>> autoComplete(CourseCriteria criteria) {
        criteria.setStatus(AIConstant.STATUS_ACTIVE);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> courses = courseRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(courses, courseMapper::fromEntityToCourseDtoAutoCompleteList), "Get auto complete Courses success");
    }
}
