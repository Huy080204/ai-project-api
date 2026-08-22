package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.notificationgroup.NotificationGroupDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.notificationgroup.CreateNotificationGroupForm;
import com.ai.api.form.notificationgroup.UpdateNotificationGroupForm;
import com.ai.api.mapper.NotificationGroupMapper;
import com.ai.api.model.Classroom;
import com.ai.api.model.NotificationGroup;
import com.ai.api.model.criteria.NotificationGroupCriteria;
import com.ai.api.repository.ClassroomRepository;
import com.ai.api.repository.NotificationGroupRepository;
import com.ai.api.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/v1/notification-group")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class NotificationGroupController extends ABasicController {
    @Autowired
    private NotificationGroupRepository notificationGroupRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private NotificationGroupMapper notificationGroupMapper;

    @Autowired
    private FileService fileService;

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NGR_V')")
    public ApiMessageDto<NotificationGroupDto> get(@PathVariable Long id) {
        NotificationGroup notificationGroup = notificationGroupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found notification group!", ErrorCode.NOTIFICATION_GROUP_ERROR_NOT_FOUND));
        return makeSuccessResponse(notificationGroupMapper.fromEntityToNotificationGroupDto(notificationGroup), "Get notification group success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NGR_L')")
    public ApiMessageDto<ResponseListDto<List<NotificationGroupDto>>> list(NotificationGroupCriteria notificationGroupCriteria,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<NotificationGroup> page = notificationGroupRepository.findAll(notificationGroupCriteria.getCriteria(), pageable);
        ResponseListDto<List<NotificationGroupDto>> responseListDto =
                makeResponseListDto(page, notificationGroupMapper::fromEntityToNotificationGroupDtoList);
        return makeSuccessResponse(responseListDto, "Get list success");
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<NotificationGroupDto>>> autoComplete(NotificationGroupCriteria criteria, Pageable pageable) {
        criteria.setStatus(AIConstant.STATUS_ACTIVE);
        Page<NotificationGroup> page = notificationGroupRepository.findAll(criteria.getCriteria(), pageable);
        return makeSuccessResponse(makeResponseListDto(page, notificationGroupMapper::fromEntityToNotificationGroupDtoAutoCompleteList),
                "Get auto complete notification groups success");
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NGR_C')")
    @Transactional
    public ApiMessageDto<NotificationGroupDto> create(@Valid @RequestBody CreateNotificationGroupForm createNotificationGroupForm, BindingResult bindingResult) {
        if (notificationGroupRepository.existsByNameAndClassroomId(createNotificationGroupForm.getName(), createNotificationGroupForm.getClassroomId())) {
            throw new BadRequestException("Notification group name already exists in this classroom", ErrorCode.NOTIFICATION_GROUP_ERROR_NAME_DUPLICATED);
        }

        Classroom classroom = classroomRepository.findById(createNotificationGroupForm.getClassroomId())
                .orElseThrow(() -> new NotFoundException("Not found classroom!", ErrorCode.CLASSROOM_ERROR_NOT_FOUND));

        NotificationGroup notificationGroup = notificationGroupMapper.fromFormToEntity(createNotificationGroupForm);
        notificationGroup.setClassroom(classroom);
        notificationGroupRepository.save(notificationGroup);
        return makeSuccessResponse(notificationGroupMapper.fromEntityToNotificationGroupIdDto(notificationGroup), "Create notification group success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NGR_U')")
    @Transactional
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateNotificationGroupForm updateNotificationGroupForm, BindingResult bindingResult) {
        NotificationGroup notificationGroup = notificationGroupRepository.findById(updateNotificationGroupForm.getId())
                .orElseThrow(() -> new NotFoundException("Not found notification group!", ErrorCode.NOTIFICATION_GROUP_ERROR_NOT_FOUND));

        if (!notificationGroup.getName().equals(updateNotificationGroupForm.getName())
                && notificationGroupRepository.existsByNameAndClassroomIdAndIdNot(updateNotificationGroupForm.getName(),
                        notificationGroup.getClassroom().getId(), notificationGroup.getId())) {
            throw new BadRequestException("Notification group name already exists in this classroom", ErrorCode.NOTIFICATION_GROUP_ERROR_NAME_DUPLICATED);
        }

        String oldAvatar = notificationGroup.getAvatar();
        notificationGroupMapper.updateEntityFromForm(updateNotificationGroupForm, notificationGroup);
        notificationGroupRepository.save(notificationGroup);

        if (oldAvatar != null && !oldAvatar.equals(updateNotificationGroupForm.getAvatar())) {
            fileService.deleteFile(oldAvatar);
        }
        return makeSuccessResponse("Update notification group success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NGR_D')")
    @Transactional
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        NotificationGroup notificationGroup = notificationGroupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found notification group!", ErrorCode.NOTIFICATION_GROUP_ERROR_NOT_FOUND));
        fileService.deleteFiles(Collections.singletonList(notificationGroup.getAvatar()));
        notificationGroupRepository.delete(notificationGroup);
        return makeSuccessResponse("Delete notification group success");
    }
}
