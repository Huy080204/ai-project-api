package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.registration.RegistrationDto;
import com.ai.api.dto.syllabus.SyllabusDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.registration.CreateRegistrationForm;
import com.ai.api.mapper.AccountMapper;
import com.ai.api.mapper.ClassroomMapper;
import com.ai.api.mapper.CourseMapper;
import com.ai.api.mapper.GroupMapper;
import com.ai.api.mapper.RegistrationMapper;
import com.ai.api.mapper.StudentMapper;
import com.ai.api.mapper.SyllabusMapper;
import com.ai.api.model.Classroom;
import com.ai.api.model.Course;
import com.ai.api.model.Registration;
import com.ai.api.model.Student;
import com.ai.api.model.Syllabus;
import com.ai.api.model.criteria.RegistrationCriteria;
import com.ai.api.repository.ClassroomRepository;
import com.ai.api.repository.ClassroomStudentRepository;
import com.ai.api.repository.RegistrationRepository;
import com.ai.api.repository.StudentRepository;
import com.ai.api.repository.SyllabusRepository;
import com.ai.api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link RegistrationController}, covering create (+ classroom-not-found,
 * classroom-not-active, duplicate-email, duplicate-phone rejections), list (classroomId filter),
 * get-by-id (not-found), and delete (success + not-found).
 */
@ExtendWith(MockitoExtension.class)
class RegistrationControllerTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private ClassroomRepository classroomRepository;

    @Mock
    private ClassroomStudentRepository classroomStudentRepository;

    @Spy
    private RegistrationMapper registrationMapper = Mappers.getMapper(RegistrationMapper.class);

    @Mock
    private StudentRepository studentRepository;

    @Spy
    private StudentMapper studentMapper = Mappers.getMapper(StudentMapper.class);

    @Mock
    private SyllabusRepository syllabusRepository;

    @Spy
    private SyllabusMapper syllabusMapper = Mappers.getMapper(SyllabusMapper.class);

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private RegistrationController registrationController;

    @BeforeEach
    void setUp() {
        // The generated Mapper impls @Autowired-inject the Mappers named in `uses = {...}`, which
        // Mappers.getMapper(...) does not do: wire registration -> classroom -> course,
        // student -> account -> group and syllabus -> course by hand.
        CourseMapper courseMapper = Mappers.getMapper(CourseMapper.class);
        ClassroomMapper classroomMapper = Mappers.getMapper(ClassroomMapper.class);
        ReflectionTestUtils.setField(classroomMapper, "courseMapper", courseMapper);
        ReflectionTestUtils.setField(registrationMapper, "classroomMapper", classroomMapper);

        AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);
        ReflectionTestUtils.setField(accountMapper, "groupMapper", Mappers.getMapper(GroupMapper.class));
        ReflectionTestUtils.setField(studentMapper, "accountMapper", accountMapper);

        ReflectionTestUtils.setField(syllabusMapper, "courseMapper", courseMapper);
    }

    private CreateRegistrationForm createForm(Long classroomId, String email, String phone) {
        CreateRegistrationForm form = new CreateRegistrationForm();
        form.setClassroomId(classroomId);
        form.setFullName("John Doe");
        form.setEmail(email);
        form.setPhone(phone);
        form.setMessage("Please contact me");
        return form;
    }

    private Classroom activeClassroom(Long id) {
        Classroom classroom = new Classroom();
        classroom.setId(id);
        classroom.setState(AIConstant.CLASSROOM_STATE_ACTIVE);
        return classroom;
    }

    // ------------------------------------------------------------------ create

    @Test
    void shouldCreateRegistrationSuccessfully() {
        // Arrange
        CreateRegistrationForm form = createForm(5L, "john@example.com", "0123456789");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        Classroom classroom = activeClassroom(5L);

        when(classroomRepository.findById(5L)).thenReturn(Optional.of(classroom));
        when(registrationRepository.existsByClassroomIdAndEmail(5L, "john@example.com")).thenReturn(false);
        when(registrationRepository.existsByClassroomIdAndPhone(5L, "0123456789")).thenReturn(false);
        when(registrationRepository.save(any(Registration.class))).thenAnswer(invocation -> {
            Registration saved = invocation.getArgument(0);
            saved.setId(11L);
            return saved;
        });

        // Act
        ApiMessageDto<RegistrationDto> result = registrationController.create(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getId()).isEqualTo(11L);
        ArgumentCaptor<Registration> registrationCaptor = ArgumentCaptor.forClass(Registration.class);
        verify(registrationRepository).save(registrationCaptor.capture());
        Registration saved = registrationCaptor.getValue();
        assertThat(saved.getFullName()).isEqualTo("John Doe");
        assertThat(saved.getEmail()).isEqualTo("john@example.com");
        assertThat(saved.getPhone()).isEqualTo("0123456789");
        assertThat(saved.getMessage()).isEqualTo("Please contact me");
        assertThat(saved.getClassroom()).isSameAs(classroom);
    }

    @Test
    void shouldThrowNotFoundWhenCreateRegistrationClassroomNotFound() {
        // Arrange
        CreateRegistrationForm form = createForm(999L, "john@example.com", "0123456789");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        when(classroomRepository.findById(999L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> registrationController.create(form, bindingResult))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_ERROR_NOT_FOUND));
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenCreateRegistrationClassroomDone() {
        // Arrange - classroom-not-active now shares CLASSROOM_ERROR_NOT_FOUND
        // (thrown as NotFoundException), not its own REGISTRATION_ERROR_CLASSROOM_NOT_ACTIVE
        CreateRegistrationForm form = createForm(5L, "john@example.com", "0123456789");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        Classroom classroom = new Classroom();
        classroom.setId(5L);
        classroom.setState(AIConstant.CLASSROOM_STATE_DONE);
        when(classroomRepository.findById(5L)).thenReturn(Optional.of(classroom));

        // Act + Assert
        assertThatThrownBy(() -> registrationController.create(form, bindingResult))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.CLASSROOM_ERROR_NOT_FOUND));
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void shouldCreateRegistrationSuccessfullyWhenClassroomIsPending() {
        // Arrange
        CreateRegistrationForm form = createForm(5L, "john@example.com", "0123456789");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        Classroom classroom = new Classroom();
        classroom.setId(5L);
        classroom.setState(AIConstant.CLASSROOM_STATE_PENDING);

        when(classroomRepository.findById(5L)).thenReturn(Optional.of(classroom));
        when(registrationRepository.existsByClassroomIdAndEmail(5L, "john@example.com")).thenReturn(false);
        when(registrationRepository.existsByClassroomIdAndPhone(5L, "0123456789")).thenReturn(false);
        when(registrationRepository.save(any(Registration.class))).thenAnswer(invocation -> {
            Registration saved = invocation.getArgument(0);
            saved.setId(12L);
            return saved;
        });

        // Act
        ApiMessageDto<RegistrationDto> result = registrationController.create(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getId()).isEqualTo(12L);
        ArgumentCaptor<Registration> registrationCaptor = ArgumentCaptor.forClass(Registration.class);
        verify(registrationRepository).save(registrationCaptor.capture());
        assertThat(registrationCaptor.getValue().getClassroom()).isSameAs(classroom);
    }

    @Test
    void shouldCreateRegistrationSuccessfullyWithNoEmail() {
        // Arrange
        CreateRegistrationForm form = createForm(5L, null, "0123456789");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        Classroom classroom = activeClassroom(5L);

        when(classroomRepository.findById(5L)).thenReturn(Optional.of(classroom));
        when(registrationRepository.existsByClassroomIdAndPhone(5L, "0123456789")).thenReturn(false);
        when(registrationRepository.save(any(Registration.class))).thenAnswer(invocation -> {
            Registration saved = invocation.getArgument(0);
            saved.setId(13L);
            return saved;
        });

        // Act
        ApiMessageDto<RegistrationDto> result = registrationController.create(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getId()).isEqualTo(13L);
        ArgumentCaptor<Registration> registrationCaptor = ArgumentCaptor.forClass(Registration.class);
        verify(registrationRepository).save(registrationCaptor.capture());
        assertThat(registrationCaptor.getValue().getClassroom()).isSameAs(classroom);
        verify(registrationRepository, never()).existsByClassroomIdAndEmail(any(), any());
        verify(classroomStudentRepository, never()).existsByClassroomIdAndStudentAccountEmail(any(), any());
    }

    @Test
    void shouldThrowBadRequestWhenCreateRegistrationDuplicateEmail() {
        // Arrange - the classroom lookup runs before the duplicate checks
        CreateRegistrationForm form = createForm(5L, "john@example.com", "0123456789");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        Classroom classroom = activeClassroom(5L);
        when(classroomRepository.findById(5L)).thenReturn(Optional.of(classroom));
        when(registrationRepository.existsByClassroomIdAndEmail(5L, "john@example.com")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> registrationController.create(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.REGISTRATION_ERROR_EMAIL_EXIST));
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenCreateRegistrationDuplicatePhone() {
        // Arrange - the classroom lookup runs before the duplicate checks
        CreateRegistrationForm form = createForm(5L, "john@example.com", "0123456789");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        Classroom classroom = activeClassroom(5L);
        when(classroomRepository.findById(5L)).thenReturn(Optional.of(classroom));
        when(registrationRepository.existsByClassroomIdAndEmail(5L, "john@example.com")).thenReturn(false);
        when(registrationRepository.existsByClassroomIdAndPhone(5L, "0123456789")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> registrationController.create(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.REGISTRATION_ERROR_PHONE_EXIST));
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenCreateRegistrationEmailAlreadyClassroomStudent() {
        // Arrange - the classroom lookup runs before the duplicate checks
        CreateRegistrationForm form = createForm(5L, "john@example.com", "0123456789");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        Classroom classroom = activeClassroom(5L);
        when(classroomRepository.findById(5L)).thenReturn(Optional.of(classroom));
        when(registrationRepository.existsByClassroomIdAndEmail(5L, "john@example.com")).thenReturn(false);
        when(classroomStudentRepository.existsByClassroomIdAndStudentAccountEmail(5L, "john@example.com")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> registrationController.create(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.REGISTRATION_ERROR_EMAIL_EXIST));
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenCreateRegistrationPhoneAlreadyClassroomStudent() {
        // Arrange - the classroom lookup runs before the duplicate checks
        CreateRegistrationForm form = createForm(5L, "john@example.com", "0123456789");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
        Classroom classroom = activeClassroom(5L);
        when(classroomRepository.findById(5L)).thenReturn(Optional.of(classroom));
        when(registrationRepository.existsByClassroomIdAndEmail(5L, "john@example.com")).thenReturn(false);
        when(registrationRepository.existsByClassroomIdAndPhone(5L, "0123456789")).thenReturn(false);
        when(classroomStudentRepository.existsByClassroomIdAndStudentAccountEmail(5L, "john@example.com")).thenReturn(false);
        when(classroomStudentRepository.existsByClassroomIdAndStudentAccountPhone(5L, "0123456789")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> registrationController.create(form, bindingResult))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.REGISTRATION_ERROR_PHONE_EXIST));
        verify(registrationRepository, never()).save(any());
    }

    // -------------------------------------------------------------------- list

    @Test
    @SuppressWarnings("unchecked")
    void shouldListRegistrationsWithClassroomIdFilter() {
        // Arrange
        RegistrationCriteria criteria = new RegistrationCriteria();
        criteria.setClassroomId(5L);
        Pageable pageable = PageRequest.of(0, 10);

        Registration registration1 = new Registration();
        registration1.setId(1L);
        registration1.setFullName("John Doe");
        Registration registration2 = new Registration();
        registration2.setId(2L);
        registration2.setFullName("Jane Doe");
        Page<Registration> page = new PageImpl<>(Arrays.asList(registration1, registration2), pageable, 2);

        when(registrationRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        ApiMessageDto<ResponseListDto<List<RegistrationDto>>> result = registrationController.list(criteria, pageable);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent())
                .extracting(RegistrationDto::getId, RegistrationDto::getFullName)
                .containsExactly(tuple(1L, "John Doe"), tuple(2L, "Jane Doe"));
        verify(registrationRepository).findAll(any(Specification.class), eq(pageable));
    }

    // --------------------------------------------------------------------- get

    @Test
    void shouldThrowNotFoundWhenGetRegistrationByIdNotFound() {
        // Arrange
        when(registrationRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> registrationController.get(99L))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.REGISTRATION_ERROR_NOT_FOUND));
    }

    @Test
    void shouldReturnStudentDtoWhenStudentFoundByPhoneOrEmail() {
        // Arrange
        Course course = new Course();
        course.setId(1L);
        Classroom classroom = new Classroom();
        classroom.setCourse(course);

        Registration registration = new Registration();
        registration.setEmail("john@example.com");
        registration.setPhone("0123456789");
        registration.setClassroom(classroom);

        Student student = new Student();
        student.setId(7L);
        student.setAddress("123 Main St");

        when(registrationRepository.findById(10L)).thenReturn(Optional.of(registration));
        when(studentRepository.findFirstByAccountPhoneOrAccountEmail("0123456789", "john@example.com"))
                .thenReturn(Optional.of(student));
        when(syllabusRepository.findByCourseIdOrderByOrderingAsc(1L)).thenReturn(List.of());

        // Act
        ApiMessageDto<RegistrationDto> result = registrationController.get(10L);

        // Assert
        assertThat(result.getData().getStudent().getId()).isEqualTo(7L);
        assertThat(result.getData().getStudent().getAddress()).isEqualTo("123 Main St");
    }

    @Test
    void shouldReturnNullStudentWhenNoStudentMatchesPhoneOrEmail() {
        // Arrange
        Course course = new Course();
        course.setId(1L);
        Classroom classroom = new Classroom();
        classroom.setCourse(course);

        Registration registration = new Registration();
        registration.setEmail("john@example.com");
        registration.setPhone("0123456789");
        registration.setClassroom(classroom);

        when(registrationRepository.findById(10L)).thenReturn(Optional.of(registration));
        when(studentRepository.findFirstByAccountPhoneOrAccountEmail("0123456789", "john@example.com"))
                .thenReturn(Optional.empty());
        when(syllabusRepository.findByCourseIdOrderByOrderingAsc(1L)).thenReturn(List.of());

        // Act
        ApiMessageDto<RegistrationDto> result = registrationController.get(10L);

        // Assert
        assertThat(result.getData().getStudent()).isNull();
    }

    @Test
    void shouldIncludeSyllabusesInMockedOrderWhenGettingRegistration() {
        // Arrange
        Course course = new Course();
        course.setId(20L);

        Classroom classroom = activeClassroom(5L);
        classroom.setCourse(course);

        Registration registration = new Registration();
        registration.setClassroom(classroom);

        Syllabus syllabus1 = new Syllabus();
        syllabus1.setId(1L);
        syllabus1.setName("Chapter 1");
        Syllabus syllabus2 = new Syllabus();
        syllabus2.setId(2L);
        syllabus2.setName("Chapter 2");
        List<Syllabus> syllabuses = Arrays.asList(syllabus1, syllabus2);

        when(registrationRepository.findById(10L)).thenReturn(Optional.of(registration));
        when(syllabusRepository.findByCourseIdOrderByOrderingAsc(20L)).thenReturn(syllabuses);

        // Act
        ApiMessageDto<RegistrationDto> result = registrationController.get(10L);

        // Assert
        assertThat(result.getData().getClassroom().getCourse().getSyllabuses())
                .extracting(SyllabusDto::getId, SyllabusDto::getName)
                .containsExactly(tuple(1L, "Chapter 1"), tuple(2L, "Chapter 2"));
    }

    // ------------------------------------------------------------------ delete

    @Test
    void shouldDeleteRegistrationSuccessfully() {
        // Arrange
        Registration registration = new Registration();
        when(registrationRepository.findById(10L)).thenReturn(Optional.of(registration));

        // Act
        ApiMessageDto<Void> result = registrationController.delete(10L);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(registrationRepository).deleteById(10L);
    }

    @Test
    void shouldThrowNotFoundWhenDeleteRegistrationNotFound() {
        // Arrange
        when(registrationRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> registrationController.delete(99L))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.REGISTRATION_ERROR_NOT_FOUND));
        verify(registrationRepository, never()).deleteById(any());
    }
}
