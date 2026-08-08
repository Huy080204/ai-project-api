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
import com.ai.api.mapper.StudentMapper;
import com.ai.api.mapper.SyllabusMapper;
import com.ai.api.model.Classroom;
import com.ai.api.model.Registration;
import com.ai.api.model.Student;
import com.ai.api.model.Syllabus;
import com.ai.api.model.Voucher;
import com.ai.api.model.criteria.RegistrationCriteria;
import com.ai.api.repository.ClassroomRepository;
import com.ai.api.repository.ClassroomStudentRepository;
import com.ai.api.repository.RegistrationRepository;
import com.ai.api.repository.StudentRepository;
import com.ai.api.repository.SyllabusRepository;
import com.ai.api.service.VoucherService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
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

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private SyllabusRepository syllabusRepository;

    @Autowired
    private SyllabusMapper syllabusMapper;

    @Autowired
    private VoucherService voucherService;

    @Transactional
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateRegistrationForm createRegistrationForm, BindingResult bindingResult) {
        Classroom classroom = classroomRepository.findById(createRegistrationForm.getClassroomId())
                .orElseThrow(() -> new NotFoundException("Classroom not found", ErrorCode.CLASSROOM_ERROR_NOT_FOUND));
        if (!AIConstant.CLASSROOM_STATE_PENDING.equals(classroom.getState()) && !AIConstant.CLASSROOM_STATE_ACTIVE.equals(classroom.getState())) {
            throw new NotFoundException("Classroom not found", ErrorCode.CLASSROOM_ERROR_NOT_FOUND);
        }

        if (StringUtils.isNotBlank(createRegistrationForm.getEmail())) {
            if (registrationRepository.existsByClassroomIdAndEmail(createRegistrationForm.getClassroomId(), createRegistrationForm.getEmail())) {
                throw new BadRequestException("Email already registered for this classroom", ErrorCode.REGISTRATION_ERROR_EMAIL_EXIST);
            }
            if (classroomStudentRepository.existsByClassroomIdAndStudentAccountEmail(createRegistrationForm.getClassroomId(), createRegistrationForm.getEmail())) {
                throw new BadRequestException("Email already registered for this classroom", ErrorCode.REGISTRATION_ERROR_EMAIL_EXIST);
            }
        }
        if (registrationRepository.existsByClassroomIdAndPhone(createRegistrationForm.getClassroomId(), createRegistrationForm.getPhone())) {
            throw new BadRequestException("Phone already registered for this classroom", ErrorCode.REGISTRATION_ERROR_PHONE_EXIST);
        }
        if (classroomStudentRepository.existsByClassroomIdAndStudentAccountPhone(createRegistrationForm.getClassroomId(), createRegistrationForm.getPhone())) {
            throw new BadRequestException("Phone already registered for this classroom", ErrorCode.REGISTRATION_ERROR_PHONE_EXIST);
        }

        Registration registration = registrationMapper.fromCreateRegistrationFormToEntity(createRegistrationForm);
        registration.setClassroom(classroom);
        if (createRegistrationForm.getVoucherId() != null) {
            BigDecimal orderValue = classroom.getPrice() != null ? classroom.getPrice() : BigDecimal.ZERO;
            Voucher voucher = voucherService.validateAndApplyVoucher(createRegistrationForm.getVoucherId(), orderValue);
            BigDecimal discountAmount = voucherService.calculateDiscountAmount(voucher, orderValue);
            registration.setVoucher(voucher);
            registration.setDiscountAmount(discountAmount);
        }
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
    public ApiMessageDto<RegistrationDto> get(@PathVariable Long id) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Registration not found", ErrorCode.REGISTRATION_ERROR_NOT_FOUND));
        RegistrationDto registrationDto = registrationMapper.fromEntityToRegistrationDto(registration);

        Student student = studentRepository.findFirstByAccountPhoneOrAccountEmail(registration.getPhone(), registration.getEmail())
                .orElse(null);
        registrationDto.setStudent(student != null ? studentMapper.fromEntityToStudentDto(student) : null);

        List<Syllabus> syllabuses = syllabusRepository.findByCourseIdOrderByOrderingAsc(registration.getClassroom().getCourse().getId());
        registrationDto.getClassroom().getCourse().setSyllabuses(syllabusMapper.fromEntityToSyllabusShortDtoList(syllabuses));
        return makeSuccessResponse(registrationDto, "Get registration success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REG_D')")
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        registrationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Registration not found", ErrorCode.REGISTRATION_ERROR_NOT_FOUND));
        registrationRepository.deleteById(id);
        return makeSuccessResponse("Delete registration success");
    }
}
