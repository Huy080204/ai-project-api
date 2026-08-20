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
import com.ai.api.repository.RegistrationRepository;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;

@RestController
@RequestMapping("/v1/class-room")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class ClassroomController extends ABasicController {
    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ClassroomMapper classroomMapper;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private ClassroomStudentRepository classroomStudentRepository;

    @Transactional
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CLR_C')")
    public ApiMessageDto<ClassroomDto> create(@Valid @RequestBody CreateClassroomForm createClassroomForm, BindingResult bindingResult) {
        Course course = courseRepository.findById(createClassroomForm.getCourseId())
                .orElseThrow(() -> new NotFoundException("Course not found", ErrorCode.COURSE_ERROR_NOT_FOUND));
        Classroom classroom = classroomMapper.fromCreateClassroomFormToEntity(createClassroomForm);
        classroom.setCourse(course);
        classroomRepository.save(classroom);
        return makeSuccessResponse(classroomMapper.fromEntityToClassroomIdDto(classroom), "Create classroom success");
    }

    @Transactional
    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CLR_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateClassroomForm updateClassroomForm, BindingResult bindingResult) {
        Classroom classroom = classroomRepository.findById(updateClassroomForm.getId())
                .orElseThrow(() -> new NotFoundException("Classroom not found", ErrorCode.CLASSROOM_ERROR_NOT_FOUND));
        if (!AIConstant.CLASSROOM_STATE_PENDING.equals(classroom.getState())) {
            throw new BadRequestException("Unable to update classroom that is not pending", ErrorCode.CLASSROOM_ERROR_UNABLE_UPDATE);
        }
        Course course = courseRepository.findById(updateClassroomForm.getCourseId())
                .orElseThrow(() -> new NotFoundException("Course not found", ErrorCode.COURSE_ERROR_NOT_FOUND));
        classroomMapper.updateEntityFromForm(updateClassroomForm, classroom);
        classroom.setCourse(course);
        classroomRepository.save(classroom);
        return makeSuccessResponse("Update classroom success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CLR_V')")
    public ApiMessageDto<ClassroomDto> get(@PathVariable("id") Long id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Classroom not found", ErrorCode.CLASSROOM_ERROR_NOT_FOUND));
        return makeSuccessResponse(classroomMapper.fromEntityToClassroomDto(classroom), "Get classroom success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CLR_L')")
    public ApiMessageDto<ResponseListDto<List<ClassroomDto>>> list(ClassroomCriteria criteria, Pageable pageable) {
        Page<Classroom> classrooms = classroomRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(classrooms, classroomMapper::fromEntityToClassroomDtoList), "List classroom success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CLR_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Classroom not found", ErrorCode.CLASSROOM_ERROR_NOT_FOUND));
        registrationRepository.deleteAllByClassroomId(id);
        classroomStudentRepository.deleteAllByClassroomId(id);
        classroomRepository.deleteById(id);
        return makeSuccessResponse("Delete classroom success");
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<ClassroomDto>>> autoComplete(ClassroomCriteria criteria) {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Classroom> classrooms = classroomRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(classrooms, classroomMapper::fromEntityToClassroomDtoAutoCompleteList), "Get auto complete classrooms success");
    }

    @GetMapping(value = "/public/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<ClassroomDto>>> publicList(ClassroomCriteria criteria, Pageable pageable) {
        criteria.setState(AIConstant.CLASSROOM_STATE_ACTIVE);
        Page<Classroom> classrooms = classroomRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(classrooms, classroomMapper::fromEntityToClassroomDtoList), "List classroom success");
    }

    @Transactional
    @PutMapping(value = "/change-state", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CLR_U')")
    public ApiMessageDto<Void> changeState(@Valid @RequestBody ChangeClassroomStateForm changeClassroomStateForm, BindingResult bindingResult) {
        Classroom classroom = classroomRepository.findById(changeClassroomStateForm.getId())
                .orElseThrow(() -> new NotFoundException("Classroom not found", ErrorCode.CLASSROOM_ERROR_NOT_FOUND));
        if (!isValidStateTransition(classroom.getState(), changeClassroomStateForm.getState())) {
            throw new BadRequestException("Invalid classroom state transition", ErrorCode.CLASSROOM_ERROR_INVALID_STATE_TRANSITION);
        }
        classroom.setState(changeClassroomStateForm.getState());
        classroomRepository.save(classroom);
        return makeSuccessResponse("Change classroom state success");
    }

    private boolean isValidStateTransition(Integer from, Integer to) {
        if (AIConstant.CLASSROOM_STATE_ACTIVE.equals(to)) {
            return AIConstant.CLASSROOM_STATE_PENDING.equals(from);
        }
        if (AIConstant.CLASSROOM_STATE_DONE.equals(to)) {
            return AIConstant.CLASSROOM_STATE_ACTIVE.equals(from);
        }
        if (AIConstant.CLASSROOM_STATE_CANCEL.equals(to)) {
            return AIConstant.CLASSROOM_STATE_PENDING.equals(from) || AIConstant.CLASSROOM_STATE_ACTIVE.equals(from);
        }
        return false;
    }
}
