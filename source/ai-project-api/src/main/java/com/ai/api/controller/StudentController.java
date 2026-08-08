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
import com.ai.api.mapper.StudentMapper;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/v1/student")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class StudentController extends ABasicController {
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private FileService fileService;
    @Autowired
    private ClassroomStudentRepository classroomStudentRepository;
    @Autowired
    private RatingRepository ratingRepository;
    @Autowired
    private SubmissionRepository submissionRepository;

    @Transactional
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('STU_C')")
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateStudentForm form, BindingResult bindingResult) {
        Group group = groupRepository.findById(form.getGroupId())
                .orElseThrow(() -> new BadRequestException("[Group] Group not found", ErrorCode.GROUP_ERROR_NOT_FOUND));

        if (group.getKind() != AIConstant.GROUP_KIND_STUDENT) {
            throw new BadRequestException("[Student] Group is not a student group", ErrorCode.STUDENT_ERROR_GROUP_KIND_INVALID);
        }

        if (accountRepository.existsByUsername(form.getUsername())) {
            throw new BadRequestException("[Account] Username exist", ErrorCode.ACCOUNT_ERROR_USERNAME_EXIST);
        }

        if (accountRepository.existsByEmailAndStatusNot(form.getEmail(), AIConstant.STATUS_DELETE)) {
            throw new BadRequestException("[Account] Email existed", ErrorCode.ACCOUNT_ERROR_EMAIL_EXISTED);
        }

        if (accountRepository.existsByPhoneAndStatusNot(form.getPhone(), AIConstant.STATUS_DELETE)) {
            throw new BadRequestException("[Account] Phone existed", ErrorCode.ACCOUNT_ERROR_PHONE_EXISTED);
        }

        Account account = studentMapper.fromFormToAccount(form);
        account.setPassword(passwordEncoder.encode(form.getPassword()));
        account.setKind(AIConstant.USER_KIND_STUDENT);
        account.setGroup(group);
        accountRepository.save(account);

        Student student = studentMapper.fromFormToEntity(form);
        student.setAccount(account);
        studentRepository.save(student);

        return makeSuccessResponse("Create student success");
    }

    @Transactional
    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('STU_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateStudentForm form, BindingResult bindingResult) {
        Student student = studentRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Student] Student not found", ErrorCode.STUDENT_ERROR_NOT_FOUND));

        Account account = student.getAccount();

        if (!Objects.equals(form.getEmail(), account.getEmail())
                && accountRepository.existsByEmailAndStatusNot(form.getEmail(), AIConstant.STATUS_DELETE)) {
            throw new BadRequestException("[Account] Email existed", ErrorCode.ACCOUNT_ERROR_EMAIL_EXISTED);
        }

        if (!Objects.equals(form.getPhone(), account.getPhone())
                && accountRepository.existsByPhoneAndStatusNot(form.getPhone(), AIConstant.STATUS_DELETE)) {
            throw new BadRequestException("[Account] Phone existed", ErrorCode.ACCOUNT_ERROR_PHONE_EXISTED);
        }

        if (!Objects.equals(form.getUsername(), account.getUsername())
                && accountRepository.existsByUsername(form.getUsername())) {
            throw new BadRequestException("[Account] Username exist", ErrorCode.ACCOUNT_ERROR_USERNAME_EXIST);
        }

        if (StringUtils.isNoneBlank(form.getFullName())) {
            account.setFullName(form.getFullName());
        }
        if (StringUtils.isNoneBlank(form.getAvatarPath())) {
            String oldAvatarPath = account.getAvatarPath();
            if (oldAvatarPath != null && !Objects.equals(form.getAvatarPath(), oldAvatarPath)) {
                fileService.deleteFile(oldAvatarPath);
            }
            account.setAvatarPath(form.getAvatarPath());
        }
        if (StringUtils.isNoneBlank(form.getEmail())) {
            account.setEmail(form.getEmail());
        }
        if (StringUtils.isNoneBlank(form.getPhone())) {
            account.setPhone(form.getPhone());
        }
        account.setUsername(form.getUsername());
        if (StringUtils.isNoneBlank(form.getPassword())) {
            account.setPassword(passwordEncoder.encode(form.getPassword()));
        }
        accountRepository.save(account);

        studentMapper.updateEntityFromForm(form, student);
        studentRepository.save(student);

        return makeSuccessResponse("Update student success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('STU_V')")
    public ApiMessageDto<StudentDto> get(@PathVariable("id") Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Student] Student not found", ErrorCode.STUDENT_ERROR_NOT_FOUND));
        return makeSuccessResponse(studentMapper.fromEntityToStudentDto(student), "Get student success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('STU_L')")
    public ApiMessageDto<ResponseListDto<List<StudentDto>>> list(StudentCriteria criteria, Pageable pageable) {
        Page<Student> students = studentRepository.findAll(criteria.getCriteria(), pageable);
        return makeSuccessResponse(makeResponseListDto(students, studentMapper::fromEntityToStudentDtoList), "List student success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('STU_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Student] Student not found", ErrorCode.STUDENT_ERROR_NOT_FOUND));

        List<String> filesToDelete = new ArrayList<>();
        String avatarPath = student.getAccount().getAvatarPath();
        if (StringUtils.isNoneBlank(avatarPath)) {
            filesToDelete.add(avatarPath);
        }
        filesToDelete.addAll(submissionRepository.findFileUrlsByStudentId(id));
        if (!filesToDelete.isEmpty()) {
            fileService.deleteFiles(filesToDelete);
        }

        classroomStudentRepository.deleteAllByStudentId(id);
        ratingRepository.deleteAllByStudentId(id);
        submissionRepository.deleteAllByStudentId(id);

        student.setStatus(AIConstant.STATUS_DELETE);
        studentRepository.save(student);
        return makeSuccessResponse("Delete student success");
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('STU_L')")
    public ApiMessageDto<ResponseListDto<List<StudentDto>>> autoComplete(StudentCriteria criteria, Pageable pageable) {
        Page<Student> students = studentRepository.findAll(criteria.getCriteria(), pageable);
        return makeSuccessResponse(makeResponseListDto(students, studentMapper::fromEntityToStudentAutoCompleteDtoList), "Get auto complete students success");
    }

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<StudentDto> profile() {
        Student student = studentRepository.findByIdAndStatus(getCurrentUser(), AIConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[Student] Student not found", ErrorCode.STUDENT_ERROR_NOT_FOUND));
        return makeSuccessResponse(studentMapper.fromEntityToStudentDto(student), "Get student profile success");
    }
}
