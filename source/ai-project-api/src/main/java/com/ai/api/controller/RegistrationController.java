package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.registration.RegistrationDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.registration.CreateRegistrationForm;
import com.ai.api.mapper.RegistrationMapper;
import com.ai.api.model.Classroom;
import com.ai.api.model.Registration;
import com.ai.api.model.criteria.RegistrationCriteria;
import com.ai.api.repository.ClassroomRepository;
import com.ai.api.repository.ClassroomStudentRepository;
import com.ai.api.repository.RegistrationRepository;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/registration")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class RegistrationController extends ABasicController {
    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private ClassroomStudentRepository classroomStudentRepository;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Transactional
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateRegistrationForm createRegistrationForm, BindingResult bindingResult) {
        Classroom classroom = classroomRepository.findById(createRegistrationForm.getClassroomId())
                .orElseThrow(() -> new NotFoundException("Classroom not found", ErrorCode.CLASSROOM_ERROR_NOT_FOUND));
        if (!AIConstant.CLASSROOM_STATE_ACTIVE.equals(classroom.getState())) {
            throw new NotFoundException("Classroom not found", ErrorCode.CLASSROOM_ERROR_NOT_FOUND);
        }

        if (registrationRepository.existsByClassroomIdAndEmail(createRegistrationForm.getClassroomId(), createRegistrationForm.getEmail())) {
            throw new BadRequestException("Email already registered for this classroom", ErrorCode.REGISTRATION_ERROR_EMAIL_EXIST);
        }
        if (registrationRepository.existsByClassroomIdAndPhone(createRegistrationForm.getClassroomId(), createRegistrationForm.getPhone())) {
            throw new BadRequestException("Phone already registered for this classroom", ErrorCode.REGISTRATION_ERROR_PHONE_EXIST);
        }
        if (classroomStudentRepository.existsByClassroom_IdAndStudent_Account_Email(createRegistrationForm.getClassroomId(), createRegistrationForm.getEmail())) {
            throw new BadRequestException("Email already registered for this classroom", ErrorCode.REGISTRATION_ERROR_EMAIL_EXIST);
        }
        if (classroomStudentRepository.existsByClassroom_IdAndStudent_Account_Phone(createRegistrationForm.getClassroomId(), createRegistrationForm.getPhone())) {
            throw new BadRequestException("Phone already registered for this classroom", ErrorCode.REGISTRATION_ERROR_PHONE_EXIST);
        }

        Registration registration = registrationMapper.fromCreateRegistrationFormToEntity(createRegistrationForm);
        registration.setClassroom(classroom);
        registrationRepository.save(registration);
        return makeSuccessResponse("Create registration success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REG_L')")
    public ApiMessageDto<ResponseListDto<List<RegistrationDto>>> list(RegistrationCriteria criteria, Pageable pageable) {
        Page<Registration> registrations = registrationRepository.findAll(criteria.getSpecification(), pageable);
        return makeSuccessResponse(makeResponseListDto(registrations, registrationMapper::fromEntityToRegistrationDtoList), "List registration success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REG_V')")
    public ApiMessageDto<RegistrationDto> get(@PathVariable("id") Long id) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Registration not found", ErrorCode.REGISTRATION_ERROR_NOT_FOUND));
        return makeSuccessResponse(registrationMapper.fromEntityToRegistrationDto(registration), "Get registration success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REG_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        registrationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Registration not found", ErrorCode.REGISTRATION_ERROR_NOT_FOUND));
        registrationRepository.deleteById(id);
        return makeSuccessResponse("Delete registration success");
    }
}
