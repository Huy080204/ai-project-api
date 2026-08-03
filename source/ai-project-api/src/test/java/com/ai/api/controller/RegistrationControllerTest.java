package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.classroom.ClassroomDto;
import com.ai.api.dto.course.CourseDto;
import com.ai.api.dto.registration.RegistrationDto;
import com.ai.api.dto.student.StudentDto;
import com.ai.api.dto.syllabus.SyllabusDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.registration.CreateRegistrationForm;
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

    @Mock
    private RegistrationMapper registrationMapper;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private SyllabusRepository syllabusRepository;

    @Mock
    private SyllabusMapper syllabusMapper;

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private RegistrationController registrationController;

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
        BindingResult bindingResult = mock(BindingResult.class);
        Classroom classroom = activeClassroom(5L);
        Registration registration = new Registration();

        when(classroomRepository.findById(5L)).thenReturn(Optional.of(classroom));
        when(registrationRepository.existsByClassroomIdAndEmail(5L, "john@example.com")).thenReturn(false);
        when(registrationRepository.existsByClassroomIdAndPhone(5L, "0123456789")).thenReturn(false);
        when(registrationMapper.fromCreateRegistrationFormToEntity(form)).thenReturn(registration);

        // Act
        ApiMessageDto<Void> result = registrationController.create(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(registration.getClassroom()).isEqualTo(classroom);
        verify(registrationRepository).save(registration);
    }

    @Test
    void shouldThrowNotFoundWhenCreateRegistrationClassroomNotFound() {
        // Arrange
        CreateRegistrationForm form = createForm(999L, "john@example.com", "0123456789");
        BindingResult bindingResult = mock(BindingResult.class);
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
        BindingResult bindingResult = mock(BindingResult.class);
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
        BindingResult bindingResult = mock(BindingResult.class);
        Classroom classroom = new Classroom();
        classroom.setId(5L);
        classroom.setState(AIConstant.CLASSROOM_STATE_PENDING);
        Registration registration = new Registration();

        when(classroomRepository.findById(5L)).thenReturn(Optional.of(classroom));
        when(registrationRepository.existsByClassroomIdAndEmail(5L, "john@example.com")).thenReturn(false);
        when(registrationRepository.existsByClassroomIdAndPhone(5L, "0123456789")).thenReturn(false);
        when(registrationMapper.fromCreateRegistrationFormToEntity(form)).thenReturn(registration);

        // Act
        ApiMessageDto<Void> result = registrationController.create(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(registration.getClassroom()).isEqualTo(classroom);
        verify(registrationRepository).save(registration);
    }

    @Test
    void shouldThrowBadRequestWhenCreateRegistrationDuplicateEmail() {
        // Arrange - the classroom lookup runs before the duplicate checks
        CreateRegistrationForm form = createForm(5L, "john@example.com", "0123456789");
        BindingResult bindingResult = mock(BindingResult.class);
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
        BindingResult bindingResult = mock(BindingResult.class);
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
        BindingResult bindingResult = mock(BindingResult.class);
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
        BindingResult bindingResult = mock(BindingResult.class);
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
        Registration registration2 = new Registration();
        Page<Registration> page = new PageImpl<>(Arrays.asList(registration1, registration2), pageable, 2);

        RegistrationDto dto1 = new RegistrationDto();
        RegistrationDto dto2 = new RegistrationDto();

        when(registrationRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(registrationMapper.fromEntityToRegistrationDtoList(anyList())).thenReturn(Arrays.asList(dto1, dto2));

        // Act
        ApiMessageDto<ResponseListDto<List<RegistrationDto>>> result = registrationController.list(criteria, pageable);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).hasSize(2);
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

        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setClassroom(new ClassroomDto());
        registrationDto.getClassroom().setCourse(new CourseDto());

        Student student = new Student();
        StudentDto studentDto = new StudentDto();

        when(registrationRepository.findById(10L)).thenReturn(Optional.of(registration));
        when(registrationMapper.fromEntityToRegistrationDto(registration)).thenReturn(registrationDto);
        when(studentRepository.findFirstByAccountPhoneOrAccountEmail("0123456789", "john@example.com"))
                .thenReturn(Optional.of(student));
        when(studentMapper.fromEntityToStudentDto(student)).thenReturn(studentDto);
        when(syllabusRepository.findByCourseIdOrderByOrderingAsc(1L)).thenReturn(List.of());

        // Act
        ApiMessageDto<RegistrationDto> result = registrationController.get(10L);

        // Assert
        assertThat(result.getData().getStudent()).isEqualTo(studentDto);
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

        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setClassroom(new ClassroomDto());
        registrationDto.getClassroom().setCourse(new CourseDto());

        when(registrationRepository.findById(10L)).thenReturn(Optional.of(registration));
        when(registrationMapper.fromEntityToRegistrationDto(registration)).thenReturn(registrationDto);
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

        CourseDto courseDto = new CourseDto();

        ClassroomDto classroomDto = new ClassroomDto();
        classroomDto.setCourse(courseDto);

        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setClassroom(classroomDto);

        Syllabus syllabus1 = new Syllabus();
        syllabus1.setId(1L);
        Syllabus syllabus2 = new Syllabus();
        syllabus2.setId(2L);
        List<Syllabus> syllabuses = Arrays.asList(syllabus1, syllabus2);

        SyllabusDto syllabusDto1 = new SyllabusDto();
        SyllabusDto syllabusDto2 = new SyllabusDto();

        when(registrationRepository.findById(10L)).thenReturn(Optional.of(registration));
        when(registrationMapper.fromEntityToRegistrationDto(registration)).thenReturn(registrationDto);
        when(syllabusRepository.findByCourseIdOrderByOrderingAsc(20L)).thenReturn(syllabuses);
        when(syllabusMapper.fromEntityToSyllabusShortDtoList(syllabuses))
                .thenReturn(Arrays.asList(syllabusDto1, syllabusDto2));

        // Act
        ApiMessageDto<RegistrationDto> result = registrationController.get(10L);

        // Assert
        assertThat(result.getData().getClassroom().getCourse().getSyllabuses())
                .hasSize(2)
                .containsExactly(syllabusDto1, syllabusDto2);
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
