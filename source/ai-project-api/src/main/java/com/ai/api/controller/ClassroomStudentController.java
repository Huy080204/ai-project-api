package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.classroomstudent.ClassroomStudentDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.classroomstudent.ChangeClassroomStudentStateForm;
import com.ai.api.form.classroomstudent.RegisterClassroomStudentForm;
import com.ai.api.form.classroomstudent.RegisterFromRegistrationForm;
import com.ai.api.mapper.ClassroomStudentMapper;
import com.ai.api.mapper.RegistrationMapper;
import com.ai.api.model.Account;
import com.ai.api.model.Classroom;
import com.ai.api.model.ClassroomStudent;
import com.ai.api.model.Group;
import com.ai.api.model.Registration;
import com.ai.api.model.Student;
import com.ai.api.model.criteria.ClassroomStudentCriteria;
import com.ai.api.repository.AccountRepository;
import com.ai.api.repository.ClassroomRepository;
import com.ai.api.repository.ClassroomStudentRepository;
import com.ai.api.repository.GroupRepository;
import com.ai.api.repository.RegistrationRepository;
import com.ai.api.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/classroom-student")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class ClassroomStudentController extends ABasicController {
    @Autowired
    private ClassroomStudentRepository classroomStudentRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ClassroomStudentMapper classroomStudentMapper;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    @PostMapping(value = "/register-by-student", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CLS_C')")
    public ApiMessageDto<Void> register(@Valid @RequestBody RegisterClassroomStudentForm registerClassroomStudentForm, BindingResult bindingResult) {
        Classroom classroom = classroomRepository.findById(registerClassroomStudentForm.getClassroomId())
                .orElseThrow(() -> new NotFoundException("Classroom not found", ErrorCode.CLASSROOM_ERROR_NOT_FOUND));
        Student student = studentRepository.findById(registerClassroomStudentForm.getStudentId())
                .orElseThrow(() -> new NotFoundException("Student not found", ErrorCode.STUDENT_ERROR_NOT_FOUND));
        if (!AIConstant.CLASSROOM_STATE_PENDING.equals(classroom.getState()) && !AIConstant.CLASSROOM_STATE_ACTIVE.equals(classroom.getState())) {
            throw new BadRequestException("Classroom is not joinable", ErrorCode.CLASSROOM_STUDENT_ERROR_CLASSROOM_NOT_JOINABLE);
        }
        if (classroomStudentRepository.existsByClassroomIdAndStudentId(classroom.getId(), student.getId())) {
            throw new BadRequestException("Student already registered to classroom", ErrorCode.CLASSROOM_STUDENT_ERROR_ALREADY_REGISTERED);
        }
        ClassroomStudent classroomStudent = classroomStudentMapper.fromFormToEntity(registerClassroomStudentForm);
        classroomStudent.setClassroom(classroom);
        classroomStudent.setStudent(student);
        classroomStudent.setDateRegistration(new Date());
        classroomStudentRepository.save(classroomStudent);
        return makeSuccessResponse("Register classroom student success");
    }

    @Transactional
    @PutMapping(value = "/change-state", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CLS_U')")
    public ApiMessageDto<Void> changeState(@Valid @RequestBody ChangeClassroomStudentStateForm changeClassroomStudentStateForm, BindingResult bindingResult) {
        ClassroomStudent classroomStudent = classroomStudentRepository.findById(changeClassroomStudentStateForm.getId())
                .orElseThrow(() -> new NotFoundException("Classroom student not found", ErrorCode.CLASSROOM_STUDENT_ERROR_NOT_FOUND));
        if (!isValidStateTransition(classroomStudent.getState(), changeClassroomStudentStateForm.getState())) {
            throw new BadRequestException("Invalid classroom student state transition", ErrorCode.CLASSROOM_STUDENT_ERROR_INVALID_STATE_TRANSITION);
        }
        classroomStudent.setState(changeClassroomStudentStateForm.getState());
        classroomStudent.setDateDone(new Date());
        classroomStudentRepository.save(classroomStudent);
        return makeSuccessResponse("Change classroom student state success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CLS_L')")
    public ApiMessageDto<ResponseListDto<List<ClassroomStudentDto>>> list(ClassroomStudentCriteria criteria, Pageable pageable) {
        Page<ClassroomStudent> classroomStudents = classroomStudentRepository.findAll(criteria.getCriteria(), pageable);
        return makeSuccessResponse(makeResponseListDto(classroomStudents, classroomStudentMapper::fromEntityToClassroomStudentDtoList), "List classroom student success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CLS_D')")
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        ClassroomStudent classroomStudent = classroomStudentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Classroom student not found", ErrorCode.CLASSROOM_STUDENT_ERROR_NOT_FOUND));
        if (!AIConstant.CLASSROOM_STUDENT_STATE_PENDING.equals(classroomStudent.getState())) {
            throw new BadRequestException("Unable to delete classroom student that is not pending", ErrorCode.CLASSROOM_STUDENT_ERROR_UNABLE_DELETE);
        }
        classroomStudentRepository.deleteById(id);
        return makeSuccessResponse("Delete classroom student success");
    }

    private boolean isValidStateTransition(Integer from, Integer to) {
        if (AIConstant.CLASSROOM_STUDENT_STATE_ACCEPT.equals(to)) {
            return AIConstant.CLASSROOM_STUDENT_STATE_PENDING.equals(from);
        }
        if (AIConstant.CLASSROOM_STUDENT_STATE_REJECT.equals(to)) {
            return AIConstant.CLASSROOM_STUDENT_STATE_PENDING.equals(from);
        }
        return false;
    }

    @Transactional
    @PostMapping(value = "/register-from-registration", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CLS_C')")
    public ApiMessageDto<Void> registerFromRegistration(@Valid @RequestBody RegisterFromRegistrationForm registerFromRegistrationForm, BindingResult bindingResult) {
        Registration registration = registrationRepository.findById(registerFromRegistrationForm.getRegistrationId())
                .orElseThrow(() -> new NotFoundException("Registration not found", ErrorCode.REGISTRATION_ERROR_NOT_FOUND));

        Student student = resolveStudent(registration);

        if (classroomStudentRepository.existsByClassroomIdAndStudentId(registration.getClassroom().getId(), student.getId())) {
            throw new BadRequestException("Student already registered to classroom", ErrorCode.CLASSROOM_STUDENT_ERROR_ALREADY_REGISTERED);
        }

        ClassroomStudent classroomStudent = new ClassroomStudent();
        classroomStudent.setClassroom(registration.getClassroom());
        classroomStudent.setStudent(student);
        classroomStudent.setDateRegistration(new Date());
        classroomStudentRepository.save(classroomStudent);

        registrationRepository.deleteById(registration.getId());

        return makeSuccessResponse("Register classroom student from registration success");
    }

    private Student resolveStudent(Registration registration) {
        Optional<Student> student = studentRepository.findFirstByAccountPhoneOrAccountEmail(registration.getPhone(), registration.getEmail());
        if (student.isPresent()) {
            return student.get();
        }

        if (accountRepository.existsByUsername(registration.getEmail())) {
            throw new BadRequestException("Username already exists", ErrorCode.ACCOUNT_ERROR_USERNAME_EXIST);
        }
        Group group = groupRepository.findFirstByKind(AIConstant.GROUP_KIND_STUDENT);
        if (group == null) {
            throw new NotFoundException("Student group not found", ErrorCode.GROUP_ERROR_NOT_FOUND);
        }
        Account account = registrationMapper.fromRegistrationToAccount(registration);
        account.setUsername(registration.getEmail());
        account.setPassword(passwordEncoder.encode(RandomStringUtils.randomAlphanumeric(12)));
        account.setKind(AIConstant.USER_KIND_STUDENT);
        account.setGroup(group);
        accountRepository.save(account);

        Student newStudent = new Student();
        newStudent.setAccount(account);
        studentRepository.save(newStudent);
        return newStudent;
    }
}
