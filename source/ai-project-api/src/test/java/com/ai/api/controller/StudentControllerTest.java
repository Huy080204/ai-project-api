package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.student.StudentDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.student.CreateStudentForm;
import com.ai.api.form.student.UpdateStudentForm;
import com.ai.api.jwt.BaseJwt;
import com.ai.api.mapper.StudentMapper;
import com.ai.api.mapper.StudentMapperImpl;
import com.ai.api.mapper.AccountMapperImpl;
import com.ai.api.model.Account;
import com.ai.api.model.Group;
import com.ai.api.model.Student;
import com.ai.api.model.criteria.StudentCriteria;
import com.ai.api.repository.AccountRepository;
import com.ai.api.repository.ClassroomStudentRepository;
import com.ai.api.repository.GroupRepository;
import com.ai.api.repository.RatingRepository;
import com.ai.api.repository.StudentRepository;
import com.ai.api.repository.SubmissionRepository;
import com.ai.api.service.FileService;
import com.ai.api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;

/**
 * Unit test for {@link StudentController#create}, written FIRST against the planned
 * `POST /v1/student/create` shape (FR-002) — {@code StudentController} does not exist yet on
 * disk, so this class is expected to fail to compile/run until it is created (red state).
 *
 * <p>Note on error-code assertions: {@code ErrorCode.STUDENT_ERROR_GROUP_KIND_INVALID} is not
 * registered yet either (that lands with the controller in the next task), so cases (b)/(c)/(d)
 * assert on the thrown exception type ({@link BadRequestException}) rather than hard-depending on
 * a not-yet-existing {@code ErrorCode} constant name.
 */
@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ClassroomStudentRepository classroomStudentRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FileService fileService;

    @Mock
    private UserServiceImpl userService;

    @InjectMocks
    private StudentController studentController;

    private CreateStudentForm createForm(String username, String email, String phone,
                                          String password, String fullName, String avatarPath,
                                          Long groupId, String address) {
        CreateStudentForm form = new CreateStudentForm();
        form.setUsername(username);
        form.setEmail(email);
        form.setPhone(phone);
        form.setPassword(password);
        form.setFullName(fullName);
        form.setAvatarPath(avatarPath);
        form.setGroupId(groupId);
        form.setAddress(address);
        return form;
    }

    // ------------------------------------------------------------------ (a) happy path

    @Test
    void shouldCreateAccountAndStudentWhenGroupIsStudentKindAndUnique() {
        // Arrange
        CreateStudentForm form = createForm("student1", "student1@example.com", "0912345678",
                "Password1!", "Student One", null, 1L, "123 Main St");

        Group group = new Group();
        group.setId(1L);
        group.setKind(AIConstant.GROUP_KIND_STUDENT);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(accountRepository.existsByUsername("student1")).thenReturn(false);
        when(accountRepository.existsByEmailAndStatusNot("student1@example.com", AIConstant.STATUS_DELETE))
                .thenReturn(false);
        when(accountRepository.existsByPhoneAndStatusNot("0912345678", AIConstant.STATUS_DELETE))
                .thenReturn(false);

        Account mappedAccount = new Account();
        when(studentMapper.fromFormToAccount(form)).thenReturn(mappedAccount);
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded-password");

        Student mappedStudent = new Student();
        when(studentMapper.fromFormToEntity(form)).thenReturn(mappedStudent);

        BindingResult bindingResult = mock(BindingResult.class);

        // Act
        ApiMessageDto<Void> result = studentController.create(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();

        assertThat(mappedAccount.getKind()).isEqualTo(AIConstant.USER_KIND_STUDENT);
        assertThat(mappedAccount.getPassword()).isEqualTo("encoded-password");
        assertThat(mappedAccount.getGroup()).isEqualTo(group);
        verify(accountRepository).save(mappedAccount);

        assertThat(mappedStudent.getAccount()).isEqualTo(mappedAccount);
        verify(studentRepository).save(mappedStudent);
    }

    // ------------------------------------------------------------- (b) wrong group kind

    @Test
    void shouldThrowBadRequestWhenGroupKindIsNotStudent() {
        // Arrange
        CreateStudentForm form = createForm("student1", "student1@example.com", "0912345678",
                "Password1!", "Student One", null, 1L, "123 Main St");

        Group group = new Group();
        group.setId(1L);
        group.setKind(AIConstant.GROUP_KIND_ADMIN);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        BindingResult bindingResult = mock(BindingResult.class);

        // Act + Assert
        assertThatThrownBy(() -> studentController.create(form, bindingResult))
                .isInstanceOf(BadRequestException.class);
        verify(accountRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(studentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    // -------------------------------------------------------------- (c) group not found

    @Test
    void shouldThrowBadRequestWhenGroupNotFound() {
        // Arrange
        CreateStudentForm form = createForm("student1", "student1@example.com", "0912345678",
                "Password1!", "Student One", null, 99L, "123 Main St");

        when(groupRepository.findById(99L)).thenReturn(Optional.empty());

        BindingResult bindingResult = mock(BindingResult.class);

        // Act + Assert
        assertThatThrownBy(() -> studentController.create(form, bindingResult))
                .isInstanceOf(BadRequestException.class);
        verify(accountRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(studentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    // ------------------------------------------------------------- (d) duplicate username

    @Test
    void shouldThrowBadRequestWhenUsernameAlreadyExists() {
        // Arrange
        CreateStudentForm form = createForm("student1", "student1@example.com", "0912345678",
                "Password1!", "Student One", null, 1L, "123 Main St");

        Group group = new Group();
        group.setId(1L);
        group.setKind(AIConstant.GROUP_KIND_STUDENT);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(accountRepository.existsByUsername("student1")).thenReturn(true);

        BindingResult bindingResult = mock(BindingResult.class);

        // Act + Assert
        assertThatThrownBy(() -> studentController.create(form, bindingResult))
                .isInstanceOf(BadRequestException.class);
        verify(accountRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(studentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    // ---------------------------------------------------------- (e) auto-complete shape (FR-007)

    @Test
    void shouldReturnAutoCompleteShapeWithAccountUsernameEmailPhoneGroupNullWhenAutoComplete() {
        // Arrange - wire up the REAL generated mapper chain (StudentMapperImpl -> AccountMapperImpl)
        // so this test proves the actual @Named delegation used by the controller, not a stub.
        StudentMapperImpl realStudentMapper = new StudentMapperImpl();
        AccountMapperImpl realAccountMapper = new AccountMapperImpl();
        ReflectionTestUtils.setField(realStudentMapper, "accountMapper", realAccountMapper);

        StudentController controller = new StudentController();
        ReflectionTestUtils.setField(controller, "studentRepository", studentRepository);
        ReflectionTestUtils.setField(controller, "studentMapper", realStudentMapper);

        Group group = new Group();
        group.setId(1L);

        Account account = new Account();
        account.setId(10L);
        account.setUsername("student1");
        account.setEmail("student1@example.com");
        account.setPhone("0912345678");
        account.setFullName("Student One");
        account.setAvatarPath("/avatar/student1.png");
        account.setGroup(group);

        Student student = new Student();
        student.setId(100L);
        student.setAddress("123 Main St");
        student.setAccount(account);

        StudentCriteria criteria = new StudentCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Student> page = new PageImpl<>(Collections.singletonList(student));

        when(studentRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // Act
        ApiMessageDto<ResponseListDto<List<StudentDto>>> result = controller.autoComplete(criteria, pageable);

        // Assert
        StudentDto studentDto = result.getData().getContent().get(0);
        assertThat(studentDto.getAccount().getUsername()).isNull();
        assertThat(studentDto.getAccount().getEmail()).isNull();
        assertThat(studentDto.getAccount().getPhone()).isNull();
        assertThat(studentDto.getAccount().getGroup()).isNull();

        assertThat(studentDto.getAccount().getId()).isEqualTo(10L);
        assertThat(studentDto.getAccount().getFullName()).isEqualTo("Student One");
        assertThat(studentDto.getAccount().getAvatarPath()).isEqualTo("/avatar/student1.png");

        assertThat(studentDto.getStatus()).isNull();
        assertThat(studentDto.getCreatedDate()).isNull();
        assertThat(studentDto.getModifiedDate()).isNull();
    }

    // ------------------------------------------------------ (f) avatar deletion on update (FR-006)

    @Test
    void shouldDeleteOldAvatarOnlyWhenUpdateAvatarPathChanges() {
        // Arrange - changed case
        UpdateStudentForm changedForm = new UpdateStudentForm();
        changedForm.setId(1L);
        changedForm.setAvatarPath("/avatar/new.png");

        Account changedAccount = new Account();
        changedAccount.setAvatarPath("/avatar/old.png");

        Student changedStudent = new Student();
        changedStudent.setId(1L);
        changedStudent.setAccount(changedAccount);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(changedStudent));

        // Act
        studentController.update(changedForm, mock(BindingResult.class));

        // Assert
        verify(fileService).deleteFile("/avatar/old.png");
        assertThat(changedAccount.getAvatarPath()).isEqualTo("/avatar/new.png");

        // Arrange - unchanged case
        UpdateStudentForm unchangedForm = new UpdateStudentForm();
        unchangedForm.setId(2L);
        unchangedForm.setAvatarPath("/avatar/same.png");

        Account unchangedAccount = new Account();
        unchangedAccount.setAvatarPath("/avatar/same.png");

        Student unchangedStudent = new Student();
        unchangedStudent.setId(2L);
        unchangedStudent.setAccount(unchangedAccount);

        when(studentRepository.findById(2L)).thenReturn(Optional.of(unchangedStudent));

        // Act
        studentController.update(unchangedForm, mock(BindingResult.class));

        // Assert - still only the one deleteFile call from the changed case above
        verify(fileService, never()).deleteFile("/avatar/same.png");
    }

    // ------------------------------------------------------ (g) avatar deletion on delete (FR-006)

    @Test
    void shouldDeleteAvatarFileOnlyWhenDeletingStudentWithNonBlankAvatarPath() {
        // Arrange - non-blank avatar path
        Account accountWithAvatar = new Account();
        accountWithAvatar.setAvatarPath("/avatar/to-delete.png");

        Student studentWithAvatar = new Student();
        studentWithAvatar.setId(1L);
        studentWithAvatar.setAccount(accountWithAvatar);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(studentWithAvatar));

        // Act
        studentController.delete(1L);

        // Assert
        verify(fileService).deleteFiles(Collections.singletonList("/avatar/to-delete.png"));
        verify(classroomStudentRepository).deleteAllByStudentId(1L);
        verify(ratingRepository).deleteAllByStudentId(1L);

        // Arrange - blank/null avatar path
        Account accountWithoutAvatar = new Account();
        accountWithoutAvatar.setAvatarPath(null);

        Student studentWithoutAvatar = new Student();
        studentWithoutAvatar.setId(2L);
        studentWithoutAvatar.setAccount(accountWithoutAvatar);

        when(studentRepository.findById(2L)).thenReturn(Optional.of(studentWithoutAvatar));

        // Act
        studentController.delete(2L);

        // Assert - still only the one deleteFile call from the non-blank case above
        verify(fileService, never()).deleteFile(null);
    }

    @Test
    void shouldCascadeDeleteSubmissionsBeforeSoftDeletingStudent() {
        Account account = new Account();
        Student student = new Student();
        student.setId(1L);
        student.setAccount(account);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        studentController.delete(1L);

        InOrder order = inOrder(submissionRepository, studentRepository);
        order.verify(submissionRepository).deleteAllByStudentId(1L);
        order.verify(studentRepository).save(student);
    }

    @Test
    void shouldCleanUpSubmissionFilesBeforeCascadeDeletingSubmissions() {
        Account account = new Account();
        Student student = new Student();
        student.setId(1L);
        student.setAccount(account);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(submissionRepository.findFileUrlsByStudentId(1L))
                .thenReturn(Collections.singletonList("submission-file.pdf"));

        studentController.delete(1L);

        InOrder order = inOrder(fileService, submissionRepository);
        order.verify(fileService).deleteFiles(Collections.singletonList("submission-file.pdf"));
        order.verify(submissionRepository).deleteAllByStudentId(1L);
    }

    @Test
    void shouldNotCallDeleteFilesWhenNoSubmissionFileUrls() {
        Account account = new Account();
        Student student = new Student();
        student.setId(1L);
        student.setAccount(account);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(submissionRepository.findFileUrlsByStudentId(1L)).thenReturn(Collections.emptyList());

        studentController.delete(1L);

        verify(fileService, never()).deleteFiles(any());
    }

    // --------------------------------------------------- (h) update sets new unique username (FR-003)

    @Test
    void shouldSetNewUsernameWhenUpdateWithNewUniqueUsername() {
        // Arrange
        Account account = new Account();
        account.setUsername("oldUsername");
        account.setEmail("student1@example.com");
        account.setPhone("0912345678");

        Student student = new Student();
        student.setId(1L);
        student.setAccount(account);

        UpdateStudentForm form = new UpdateStudentForm();
        form.setId(1L);
        form.setEmail("student1@example.com");
        form.setPhone("0912345678");
        form.setUsername("newUsername");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(accountRepository.existsByUsername("newUsername")).thenReturn(false);

        // Act
        studentController.update(form, mock(BindingResult.class));

        // Assert
        assertThat(account.getUsername()).isEqualTo("newUsername");
        verify(accountRepository).save(account);
    }

    // ------------------------------------------- (i) update with colliding username throws (FR-003)

    @Test
    void shouldThrowBadRequestWhenUpdateWithCollidingUsername() {
        // Arrange
        Account account = new Account();
        account.setUsername("oldUsername");
        account.setEmail("student1@example.com");
        account.setPhone("0912345678");

        Student student = new Student();
        student.setId(1L);
        student.setAccount(account);

        UpdateStudentForm form = new UpdateStudentForm();
        form.setId(1L);
        form.setEmail("student1@example.com");
        form.setPhone("0912345678");
        form.setUsername("takenUsername");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(accountRepository.existsByUsername("takenUsername")).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> studentController.update(form, mock(BindingResult.class)))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.ACCOUNT_ERROR_USERNAME_EXIST));
        verify(accountRepository, never()).save(any());
        verify(studentRepository, never()).save(any());
    }

    // -------------------------------------------- (j) update with unchanged username skips check (FR-009)

    @Test
    void shouldNotCallExistsByUsernameWhenUpdateWithUsernameEqualToCurrentValue() {
        // Arrange
        Account account = new Account();
        account.setUsername("sameUsername");
        account.setEmail("student1@example.com");
        account.setPhone("0912345678");

        Student student = new Student();
        student.setId(1L);
        student.setAccount(account);

        UpdateStudentForm form = new UpdateStudentForm();
        form.setId(1L);
        form.setEmail("student1@example.com");
        form.setPhone("0912345678");
        form.setUsername("sameUsername");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        // Act
        studentController.update(form, mock(BindingResult.class));

        // Assert
        verify(accountRepository, never()).existsByUsername(any());
    }

    // ---------------------------------------------- (k) update with non-blank password encodes it (FR-003)

    @Test
    void shouldEncodeAndSetPasswordWhenUpdateWithNonBlankPassword() {
        // Arrange
        Account account = new Account();
        account.setUsername("sameUsername");
        account.setEmail("student1@example.com");
        account.setPhone("0912345678");
        account.setPassword("oldEncodedPassword");

        Student student = new Student();
        student.setId(1L);
        student.setAccount(account);

        UpdateStudentForm form = new UpdateStudentForm();
        form.setId(1L);
        form.setEmail("student1@example.com");
        form.setPhone("0912345678");
        form.setUsername("sameUsername");
        form.setPassword("NewPassword1!");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(passwordEncoder.encode("NewPassword1!")).thenReturn("newEncodedPassword");

        // Act
        studentController.update(form, mock(BindingResult.class));

        // Assert
        verify(passwordEncoder).encode("NewPassword1!");
        assertThat(account.getPassword()).isEqualTo("newEncodedPassword");
    }

    // ---------------------------------------- (l) update with blank/absent password leaves it (FR-003)

    @Test
    void shouldLeavePasswordUntouchedWhenUpdateWithBlankPassword() {
        // Arrange
        Account account = new Account();
        account.setUsername("sameUsername");
        account.setEmail("student1@example.com");
        account.setPhone("0912345678");
        account.setPassword("oldEncodedPassword");

        Student student = new Student();
        student.setId(1L);
        student.setAccount(account);

        UpdateStudentForm form = new UpdateStudentForm();
        form.setId(1L);
        form.setEmail("student1@example.com");
        form.setPhone("0912345678");
        form.setUsername("sameUsername");
        form.setPassword(null);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        // Act
        studentController.update(form, mock(BindingResult.class));

        // Assert
        verify(passwordEncoder, never()).encode(any());
        assertThat(account.getPassword()).isEqualTo("oldEncodedPassword");
    }

    // ---------------------------------------------- (m) profile() returns current user's StudentDto

    @Test
    void shouldReturnCurrentUserStudentDtoWhenProfileResolves() {
        // Arrange
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(5L);
        when(userService.getAddInfoFromToken()).thenReturn(jwt);

        Account account = new Account();
        account.setId(5L);

        Student student = new Student();
        student.setId(5L);
        student.setAccount(account);

        StudentDto studentDto = new StudentDto();

        when(studentRepository.findByIdAndStatus(5L, AIConstant.STATUS_ACTIVE)).thenReturn(Optional.of(student));
        when(studentMapper.fromEntityToStudentDto(student)).thenReturn(studentDto);

        // Act
        ApiMessageDto<StudentDto> result = studentController.profile();

        // Assert
        assertThat(result.getData()).isEqualTo(studentDto);
    }

    // ------------------------------------------- (n) profile() throws NotFoundException when missing

    @Test
    void shouldThrowNotFoundWhenProfileDoesNotResolve() {
        // Arrange
        BaseJwt jwt = new BaseJwt();
        jwt.setAccountId(5L);
        when(userService.getAddInfoFromToken()).thenReturn(jwt);

        when(studentRepository.findByIdAndStatus(5L, AIConstant.STATUS_ACTIVE)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> studentController.profile())
                .isInstanceOf(NotFoundException.class);
    }
}
