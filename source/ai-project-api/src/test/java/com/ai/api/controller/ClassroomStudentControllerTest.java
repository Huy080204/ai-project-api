package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.classroomstudent.ClassroomStudentDto;
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
import com.ai.api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ClassroomStudentController}.
 *
 * <p>Only the {@code register*} cases are covered here (task T003 / FR-002). The
 * {@code change-state}/{@code list}/{@code delete} cases are added by later batches
 * (B5, B7) directly into this same test class.
 */
@ExtendWith(MockitoExtension.class)
class ClassroomStudentControllerTest {

    @Mock
    private ClassroomStudentRepository classroomStudentRepository;

    @Mock
    private ClassroomRepository classroomRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ClassroomStudentMapper classroomStudentMapper;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private RegistrationMapper registrationMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private ClassroomStudentController classroomStudentController;

    private RegisterClassroomStudentForm registerForm(Long classroomId, Long studentId) {
        RegisterClassroomStudentForm form = new RegisterClassroomStudentForm();
        form.setClassroomId(classroomId);
        form.setStudentId(studentId);
        return form;
    }

    private RegisterFromRegistrationForm registerFromRegistrationForm(Long registrationId) {
        RegisterFromRegistrationForm form = new RegisterFromRegistrationForm();
        form.setRegistrationId(registrationId);
        return form;
    }

    private ChangeClassroomStudentStateForm changeStateForm(Long id, Integer state) {
        ChangeClassroomStudentStateForm form = new ChangeClassroomStudentStateForm();
        form.setId(id);
        form.setState(state);
        return form;
    }

    // ---------------------------------------------------------------- register

    @Test
    void shouldRegisterClassroomStudentSuccessfully() {
        // Arrange
        RegisterClassroomStudentForm form = registerForm(1L, 2L);
        BindingResult bindingResult = mock(BindingResult.class);
        Classroom classroom = new Classroom();
        classroom.setId(1L);
        classroom.setState(AIConstant.CLASSROOM_STATE_PENDING);
        Student student = new Student();
        student.setId(2L);
        ClassroomStudent classroomStudent = new ClassroomStudent();

        when(classroomRepository.findById(1L)).thenReturn(Optional.of(classroom));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(classroomStudentRepository.existsByClassroom_IdAndStudent_Id(1L, 2L)).thenReturn(false);
        when(classroomStudentMapper.fromFormToEntity(form)).thenReturn(classroomStudent);

        // Act
        ApiMessageDto<Void> result = classroomStudentController.register(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(classroomStudent.getClassroom()).isEqualTo(classroom);
        assertThat(classroomStudent.getStudent()).isEqualTo(student);
        assertThat(classroomStudent.getState()).isEqualTo(AIConstant.CLASSROOM_STUDENT_STATE_PENDING);
        assertThat(classroomStudent.getDateRegistration()).isNotNull();
        assertThat(classroomStudent.getDateDone()).isNull();
        verify(classroomStudentRepository).save(classroomStudent);
    }

    @Test
    void shouldThrowNotFoundWhenRegisterClassroomMissing() {
        // Arrange
        RegisterClassroomStudentForm form = registerForm(999L, 2L);
        BindingResult bindingResult = mock(BindingResult.class);
        when(classroomRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> classroomStudentController.register(form, bindingResult))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_ERROR_NOT_FOUND));
        verify(classroomStudentRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenRegisterStudentMissing() {
        // Arrange
        RegisterClassroomStudentForm form = registerForm(1L, 999L);
        BindingResult bindingResult = mock(BindingResult.class);
        Classroom classroom = new Classroom();
        classroom.setId(1L);
        classroom.setState(AIConstant.CLASSROOM_STATE_PENDING);
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(classroom));
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> classroomStudentController.register(form, bindingResult))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.STUDENT_ERROR_NOT_FOUND));
        verify(classroomStudentRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenRegisterClassroomStateDone() {
        // Arrange
        RegisterClassroomStudentForm form = registerForm(1L, 2L);
        BindingResult bindingResult = mock(BindingResult.class);
        Classroom classroom = new Classroom();
        classroom.setId(1L);
        classroom.setState(AIConstant.CLASSROOM_STATE_DONE);
        Student student = new Student();
        student.setId(2L);
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(classroom));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));

        // Act + Assert
        assertThatThrownBy(() -> classroomStudentController.register(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_STUDENT_ERROR_CLASSROOM_NOT_JOINABLE));
        verify(classroomStudentRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenRegisterClassroomStateCancel() {
        // Arrange
        RegisterClassroomStudentForm form = registerForm(1L, 2L);
        BindingResult bindingResult = mock(BindingResult.class);
        Classroom classroom = new Classroom();
        classroom.setId(1L);
        classroom.setState(AIConstant.CLASSROOM_STATE_CANCEL);
        Student student = new Student();
        student.setId(2L);
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(classroom));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));

        // Act + Assert
        assertThatThrownBy(() -> classroomStudentController.register(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_STUDENT_ERROR_CLASSROOM_NOT_JOINABLE));
        verify(classroomStudentRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenRegisterDuplicatePair() {
        // Arrange - duplicate pair rejected regardless of the existing row's state
        RegisterClassroomStudentForm form = registerForm(1L, 2L);
        BindingResult bindingResult = mock(BindingResult.class);
        Classroom classroom = new Classroom();
        classroom.setId(1L);
        classroom.setState(AIConstant.CLASSROOM_STATE_PENDING);
        Student student = new Student();
        student.setId(2L);
        when(classroomRepository.findById(1L)).thenReturn(Optional.of(classroom));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(classroomStudentRepository.existsByClassroom_IdAndStudent_Id(1L, 2L)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> classroomStudentController.register(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_STUDENT_ERROR_ALREADY_REGISTERED));
        verify(classroomStudentRepository, never()).save(any());
    }

    // ------------------------------------------------------------- changeState

    @Test
    void shouldChangeStateForValidTransitionPendingToAccept() {
        // Arrange
        ChangeClassroomStudentStateForm form = changeStateForm(1L, AIConstant.CLASSROOM_STUDENT_STATE_ACCEPT);
        BindingResult bindingResult = mock(BindingResult.class);
        ClassroomStudent classroomStudent = new ClassroomStudent();
        classroomStudent.setId(1L);
        classroomStudent.setState(AIConstant.CLASSROOM_STUDENT_STATE_PENDING);
        when(classroomStudentRepository.findById(1L)).thenReturn(Optional.of(classroomStudent));

        // Act
        ApiMessageDto<Void> result = classroomStudentController.changeState(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(classroomStudent.getState()).isEqualTo(AIConstant.CLASSROOM_STUDENT_STATE_ACCEPT);
        assertThat(classroomStudent.getDateDone()).isNotNull();
        verify(classroomStudentRepository).save(classroomStudent);
    }

    @Test
    void shouldChangeStateForValidTransitionPendingToReject() {
        // Arrange
        ChangeClassroomStudentStateForm form = changeStateForm(1L, AIConstant.CLASSROOM_STUDENT_STATE_REJECT);
        BindingResult bindingResult = mock(BindingResult.class);
        ClassroomStudent classroomStudent = new ClassroomStudent();
        classroomStudent.setId(1L);
        classroomStudent.setState(AIConstant.CLASSROOM_STUDENT_STATE_PENDING);
        when(classroomStudentRepository.findById(1L)).thenReturn(Optional.of(classroomStudent));

        // Act
        ApiMessageDto<Void> result = classroomStudentController.changeState(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(classroomStudent.getState()).isEqualTo(AIConstant.CLASSROOM_STUDENT_STATE_REJECT);
        assertThat(classroomStudent.getDateDone()).isNotNull();
        verify(classroomStudentRepository).save(classroomStudent);
    }

    @Test
    void shouldThrowBadRequestWhenChangeStateOutOfAlreadyDecidedState() {
        // Arrange - accept(1) is already decided, no further transition is permitted
        ChangeClassroomStudentStateForm form = changeStateForm(1L, AIConstant.CLASSROOM_STUDENT_STATE_REJECT);
        BindingResult bindingResult = mock(BindingResult.class);
        ClassroomStudent classroomStudent = new ClassroomStudent();
        classroomStudent.setId(1L);
        classroomStudent.setState(AIConstant.CLASSROOM_STUDENT_STATE_ACCEPT);
        when(classroomStudentRepository.findById(1L)).thenReturn(Optional.of(classroomStudent));

        // Act + Assert
        assertThatThrownBy(() -> classroomStudentController.changeState(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_STUDENT_ERROR_INVALID_STATE_TRANSITION));
        assertThat(classroomStudent.getState()).isEqualTo(AIConstant.CLASSROOM_STUDENT_STATE_ACCEPT);
        verify(classroomStudentRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenChangeStateIsNoOp() {
        // Arrange - pending(0) -> pending(0) is not a permitted transition
        ChangeClassroomStudentStateForm form = changeStateForm(1L, AIConstant.CLASSROOM_STUDENT_STATE_PENDING);
        BindingResult bindingResult = mock(BindingResult.class);
        ClassroomStudent classroomStudent = new ClassroomStudent();
        classroomStudent.setId(1L);
        classroomStudent.setState(AIConstant.CLASSROOM_STUDENT_STATE_PENDING);
        when(classroomStudentRepository.findById(1L)).thenReturn(Optional.of(classroomStudent));

        // Act + Assert
        assertThatThrownBy(() -> classroomStudentController.changeState(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_STUDENT_ERROR_INVALID_STATE_TRANSITION));
        verify(classroomStudentRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenChangeStateIdMissing() {
        // Arrange
        ChangeClassroomStudentStateForm form = changeStateForm(999L, AIConstant.CLASSROOM_STUDENT_STATE_ACCEPT);
        BindingResult bindingResult = mock(BindingResult.class);
        when(classroomStudentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> classroomStudentController.changeState(form, bindingResult))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_STUDENT_ERROR_NOT_FOUND));
        verify(classroomStudentRepository, never()).save(any());
    }

    // ------------------------------------------------------------------- list

    @Test
    @SuppressWarnings("unchecked")
    void shouldListClassroomStudentsFilteredByClassroomId() {
        // Arrange
        ClassroomStudentCriteria criteria = new ClassroomStudentCriteria();
        criteria.setClassroomId(5L);
        Pageable pageable = PageRequest.of(0, 10);

        ClassroomStudent classroomStudent1 = new ClassroomStudent();
        classroomStudent1.setId(1L);
        ClassroomStudent classroomStudent2 = new ClassroomStudent();
        classroomStudent2.setId(2L);
        Page<ClassroomStudent> page = new PageImpl<>(Arrays.asList(classroomStudent1, classroomStudent2), pageable, 2);

        ClassroomStudentDto dto1 = new ClassroomStudentDto();
        dto1.setId(1L);
        ClassroomStudentDto dto2 = new ClassroomStudentDto();
        dto2.setId(2L);

        when(classroomStudentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(classroomStudentMapper.fromEntityToClassroomStudentDtoList(anyList())).thenReturn(Arrays.asList(dto1, dto2));

        // Act
        ApiMessageDto<ResponseListDto<List<ClassroomStudentDto>>> result = classroomStudentController.list(criteria, pageable);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).hasSize(2);
        verify(classroomStudentRepository).findAll(any(Specification.class), eq(pageable));
    }

    // ----------------------------------------------------------------- delete

    @Test
    void shouldDeleteClassroomStudentSuccessfullyWhenStatePending() {
        // Arrange
        ClassroomStudent classroomStudent = new ClassroomStudent();
        classroomStudent.setId(10L);
        classroomStudent.setState(AIConstant.CLASSROOM_STUDENT_STATE_PENDING);
        when(classroomStudentRepository.findById(10L)).thenReturn(Optional.of(classroomStudent));

        // Act
        ApiMessageDto<Void> result = classroomStudentController.delete(10L);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(classroomStudentRepository).deleteById(10L);
    }

    @Test
    void shouldThrowBadRequestWhenDeleteClassroomStudentStateNotPending() {
        // Arrange
        ClassroomStudent classroomStudent = new ClassroomStudent();
        classroomStudent.setId(11L);
        classroomStudent.setState(AIConstant.CLASSROOM_STUDENT_STATE_ACCEPT);
        when(classroomStudentRepository.findById(11L)).thenReturn(Optional.of(classroomStudent));

        // Act + Assert
        assertThatThrownBy(() -> classroomStudentController.delete(11L))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_STUDENT_ERROR_UNABLE_DELETE));
        verify(classroomStudentRepository, never()).deleteById(any());
    }

    @Test
    void shouldThrowNotFoundWhenDeleteClassroomStudentNotFound() {
        // Arrange
        when(classroomStudentRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> classroomStudentController.delete(99L))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_STUDENT_ERROR_NOT_FOUND));
        verify(classroomStudentRepository, never()).deleteById(any());
    }

    // ---- register-from-registration

    private Registration registration(Long id, Long classroomId, String email, String phone) {
        Registration registration = new Registration();
        registration.setId(id);
        Classroom classroom = new Classroom();
        classroom.setId(classroomId);
        registration.setClassroom(classroom);
        registration.setFullName("Registrant Name");
        registration.setEmail(email);
        registration.setPhone(phone);
        return registration;
    }

    @Test
    void shouldReuseExistingStudentWhenEmailMatches() {
        // Arrange
        RegisterFromRegistrationForm form = registerFromRegistrationForm(1L);
        BindingResult bindingResult = mock(BindingResult.class);
        Registration registration = registration(1L, 5L, "existing@example.com", "0911111111");

        Account existingAccount = new Account();
        existingAccount.setId(20L);
        existingAccount.setEmail("existing@example.com");
        Student existingStudent = new Student();
        existingStudent.setId(20L);
        existingStudent.setAccount(existingAccount);

        when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));
        when(studentRepository.findFirstByAccountPhoneOrAccountEmail("0911111111", "existing@example.com")).thenReturn(Optional.of(existingStudent));
        when(classroomStudentRepository.existsByClassroom_IdAndStudent_Id(5L, 20L)).thenReturn(false);

        // Act
        ApiMessageDto<Void> result = classroomStudentController.registerFromRegistration(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(accountRepository, never()).save(any());
        verify(studentRepository, never()).save(any());
        verify(classroomStudentRepository).save(any(ClassroomStudent.class));
        verify(registrationRepository).deleteById(1L);
    }

    @Test
    void shouldReuseExistingStudentWhenPhoneMatchesAsFallback() {
        // Arrange
        RegisterFromRegistrationForm form = registerFromRegistrationForm(1L);
        BindingResult bindingResult = mock(BindingResult.class);
        Registration registration = registration(1L, 5L, "unknown@example.com", "0922222222");

        Account existingAccount = new Account();
        existingAccount.setId(21L);
        existingAccount.setPhone("0922222222");
        Student existingStudent = new Student();
        existingStudent.setId(21L);
        existingStudent.setAccount(existingAccount);

        when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));
        when(studentRepository.findFirstByAccountPhoneOrAccountEmail("0922222222", "unknown@example.com")).thenReturn(Optional.of(existingStudent));
        when(classroomStudentRepository.existsByClassroom_IdAndStudent_Id(5L, 21L)).thenReturn(false);

        // Act
        ApiMessageDto<Void> result = classroomStudentController.registerFromRegistration(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(accountRepository, never()).save(any());
        verify(studentRepository, never()).save(any());
        verify(classroomStudentRepository).save(any(ClassroomStudent.class));
        verify(registrationRepository).deleteById(1L);
    }

    @Test
    void shouldCreateNewAccountAndStudentWhenNoMatch() {
        // Arrange
        RegisterFromRegistrationForm form = registerFromRegistrationForm(1L);
        BindingResult bindingResult = mock(BindingResult.class);
        Registration registration = registration(1L, 5L, "new@example.com", "0933333333");

        Group studentGroup = new Group();
        studentGroup.setId(9L);
        studentGroup.setKind(AIConstant.GROUP_KIND_STUDENT);

        Account mappedAccount = new Account();

        when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));
        when(studentRepository.findFirstByAccountPhoneOrAccountEmail("0933333333", "new@example.com")).thenReturn(Optional.empty());
        when(accountRepository.existsByUsername("new@example.com")).thenReturn(false);
        when(groupRepository.findFirstByKind(AIConstant.GROUP_KIND_STUDENT)).thenReturn(studentGroup);
        when(registrationMapper.fromRegistrationToAccount(registration)).thenReturn(mappedAccount);
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");

        // Act
        ApiMessageDto<Void> result = classroomStudentController.registerFromRegistration(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(mappedAccount.getUsername()).isEqualTo("new@example.com");
        assertThat(mappedAccount.getKind()).isEqualTo(AIConstant.USER_KIND_STUDENT);
        assertThat(mappedAccount.getPassword()).isEqualTo("encoded-password");
        assertThat(mappedAccount.getGroup()).isEqualTo(studentGroup);
        verify(accountRepository).save(mappedAccount);
        verify(studentRepository).save(any(Student.class));
        verify(classroomStudentRepository).save(any(ClassroomStudent.class));
        verify(registrationRepository).deleteById(1L);
    }

    @Test
    void shouldThrowNotFoundWhenRegistrationMissing() {
        // Arrange
        RegisterFromRegistrationForm form = registerFromRegistrationForm(999L);
        BindingResult bindingResult = mock(BindingResult.class);
        when(registrationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> classroomStudentController.registerFromRegistration(form, bindingResult))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.REGISTRATION_ERROR_NOT_FOUND));
        verify(classroomStudentRepository, never()).save(any());
        verify(registrationRepository, never()).deleteById(any());
    }

    @Test
    void shouldThrowBadRequestWhenUsernameAlreadyExistsOnCreatePath() {
        // Arrange
        RegisterFromRegistrationForm form = registerFromRegistrationForm(1L);
        BindingResult bindingResult = mock(BindingResult.class);
        Registration registration = registration(1L, 5L, "dup@example.com", "0944444444");

        when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));
        when(studentRepository.findFirstByAccountPhoneOrAccountEmail("0944444444", "dup@example.com")).thenReturn(Optional.empty());
        when(accountRepository.existsByUsername("dup@example.com")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> classroomStudentController.registerFromRegistration(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.ACCOUNT_ERROR_USERNAME_EXIST));
        verify(accountRepository, never()).save(any());
        verify(studentRepository, never()).save(any());
        verify(classroomStudentRepository, never()).save(any());
        verify(registrationRepository, never()).deleteById(any());
    }

    @Test
    void shouldThrowNotFoundWhenNoStudentGroupConfigured() {
        // Arrange
        RegisterFromRegistrationForm form = registerFromRegistrationForm(1L);
        BindingResult bindingResult = mock(BindingResult.class);
        Registration registration = registration(1L, 5L, "nogroup@example.com", "0955555555");

        when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));
        when(studentRepository.findFirstByAccountPhoneOrAccountEmail("0955555555", "nogroup@example.com")).thenReturn(Optional.empty());
        when(accountRepository.existsByUsername("nogroup@example.com")).thenReturn(false);
        when(groupRepository.findFirstByKind(AIConstant.GROUP_KIND_STUDENT)).thenReturn(null);

        // Act + Assert
        assertThatThrownBy(() -> classroomStudentController.registerFromRegistration(form, bindingResult))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.GROUP_ERROR_NOT_FOUND));
        verify(accountRepository, never()).save(any());
        verify(studentRepository, never()).save(any());
        verify(classroomStudentRepository, never()).save(any());
        verify(registrationRepository, never()).deleteById(any());
    }

    @Test
    void shouldThrowBadRequestWhenAlreadyRegistered() {
        // Arrange
        RegisterFromRegistrationForm form = registerFromRegistrationForm(1L);
        BindingResult bindingResult = mock(BindingResult.class);
        Registration registration = registration(1L, 5L, "already@example.com", "0966666666");

        Account existingAccount = new Account();
        existingAccount.setId(22L);
        existingAccount.setEmail("already@example.com");
        Student existingStudent = new Student();
        existingStudent.setId(22L);
        existingStudent.setAccount(existingAccount);

        when(registrationRepository.findById(1L)).thenReturn(Optional.of(registration));
        when(studentRepository.findFirstByAccountPhoneOrAccountEmail("0966666666", "already@example.com")).thenReturn(Optional.of(existingStudent));
        when(classroomStudentRepository.existsByClassroom_IdAndStudent_Id(5L, 22L)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> classroomStudentController.registerFromRegistration(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_STUDENT_ERROR_ALREADY_REGISTERED));
        verify(classroomStudentRepository, never()).save(any());
        verify(registrationRepository, never()).deleteById(any());
    }
}
