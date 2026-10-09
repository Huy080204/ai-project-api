package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.mentor.MentorDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.mentor.CreateMentorForm;
import com.ai.api.form.mentor.UpdateMentorForm;
import com.ai.api.mapper.MentorMapper;
import com.ai.api.model.Account;
import com.ai.api.model.Group;
import com.ai.api.model.Mentor;
import com.ai.api.model.criteria.MentorCriteria;
import com.ai.api.repository.AccountRepository;
import com.ai.api.repository.GroupRepository;
import com.ai.api.repository.MentorRepository;
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
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/v1/mentor")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class MentorController extends ABasicController {
    @Autowired
    private MentorRepository mentorRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private MentorMapper mentorMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private FileService fileService;

    @Transactional
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MEN_C')")
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateMentorForm form, BindingResult bindingResult) {
        Group group = groupRepository.findById(form.getGroupId())
                .orElseThrow(() -> new BadRequestException("[Group] Group not found", ErrorCode.GROUP_ERROR_NOT_FOUND));

        if (group.getKind() != AIConstant.GROUP_KIND_MENTOR) {
            throw new BadRequestException("[Mentor] Group is not a mentor group", ErrorCode.MENTOR_ERROR_GROUP_KIND_INVALID);
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

        Account account = mentorMapper.fromFormToAccount(form);
        account.setPassword(passwordEncoder.encode(form.getPassword()));
        account.setKind(AIConstant.USER_KIND_MENTOR);
        account.setGroup(group);
        accountRepository.save(account);

        Mentor mentor = mentorMapper.fromFormToEntity(form);
        mentor.setAccount(account);
        mentorRepository.save(mentor);

        return makeSuccessResponse("Create mentor success");
    }

    @Transactional
    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MEN_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateMentorForm form, BindingResult bindingResult) {
        Mentor mentor = mentorRepository.findById(form.getId())
                .orElseThrow(() -> new NotFoundException("[Mentor] Mentor not found", ErrorCode.MENTOR_ERROR_NOT_FOUND));

        Account account = mentor.getAccount();

        if (!form.getEmail().equals(account.getEmail())
                && accountRepository.existsByEmailAndStatusNot(form.getEmail(), AIConstant.STATUS_DELETE)) {
            throw new BadRequestException("[Account] Email existed", ErrorCode.ACCOUNT_ERROR_EMAIL_EXISTED);
        }

        if (!form.getPhone().equals(account.getPhone())
                && accountRepository.existsByPhoneAndStatusNot(form.getPhone(), AIConstant.STATUS_DELETE)) {
            throw new BadRequestException("[Account] Phone existed", ErrorCode.ACCOUNT_ERROR_PHONE_EXISTED);
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
        accountRepository.save(account);

        mentorMapper.updateEntityFromForm(form, mentor);
        mentorRepository.save(mentor);

        return makeSuccessResponse("Update mentor success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MEN_V')")
    public ApiMessageDto<MentorDto> get(@PathVariable(value = "id") Long id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Mentor] Mentor not found", ErrorCode.MENTOR_ERROR_NOT_FOUND));
        return makeSuccessResponse(mentorMapper.fromEntityToMentorDto(mentor), "Get mentor success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MEN_L')")
    public ApiMessageDto<ResponseListDto<List<MentorDto>>> list(MentorCriteria criteria, Pageable pageable) {
        Page<Mentor> mentors = mentorRepository.findAll(criteria.getCriteria(), pageable);
        return makeSuccessResponse(makeResponseListDto(mentors, mentorMapper::fromEntityToMentorDtoList), "List mentor success");
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MEN_L')")
    public ApiMessageDto<ResponseListDto<List<MentorDto>>> autoComplete(MentorCriteria criteria, Pageable pageable) {
        Page<Mentor> mentors = mentorRepository.findAll(criteria.getCriteria(), pageable);
        return makeSuccessResponse(makeResponseListDto(mentors, mentorMapper::fromEntityToMentorAutoCompleteDtoList), "Get auto complete mentors success");
    }

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<MentorDto> profile() {
        Mentor mentor = mentorRepository.findByIdAndStatus(getCurrentUser(), AIConstant.STATUS_ACTIVE)
                .orElseThrow(() -> new NotFoundException("[Mentor] Mentor not found", ErrorCode.MENTOR_ERROR_NOT_FOUND));
        return makeSuccessResponse(mentorMapper.fromEntityToMentorDto(mentor), "Get mentor profile success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('MEN_D')")
    public ApiMessageDto<Void> delete(@PathVariable(value = "id") Long id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("[Mentor] Mentor not found", ErrorCode.MENTOR_ERROR_NOT_FOUND));

        String avatarPath = mentor.getAccount().getAvatarPath();
        if (StringUtils.isNoneBlank(avatarPath)) {
            fileService.deleteFile(avatarPath);
        }

        mentor.setStatus(AIConstant.STATUS_DELETE);
        mentorRepository.save(mentor);
        return makeSuccessResponse("Delete mentor success");
    }

    @GetMapping(value = "/public/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<MentorDto>>> publicList(MentorCriteria criteria, Pageable pageable) {
        criteria.setStatus(AIConstant.STATUS_ACTIVE);
        Page<Mentor> mentors = mentorRepository.findAll(criteria.getCriteria(), pageable);
        return makeSuccessResponse(makeResponseListDto(mentors, mentorMapper::fromEntityToMentorDtoPublicList), "List mentor success");
    }
}
