package com.ai.api.controller;

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
import org.springframework.data.jpa.domain.Specification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationGroupControllerTest {

    @Mock
    private NotificationGroupRepository notificationGroupRepository;
    @Mock
    private ClassroomRepository classroomRepository;
    @Mock
    private NotificationGroupMapper notificationGroupMapper;
    @Mock
    private FileService fileService;
    @InjectMocks
    private NotificationGroupController controller;

    private final Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));

    @Test
    void shouldReturnNotificationGroupDtoWhenIdExists() {
        NotificationGroup entity = new NotificationGroup();
        NotificationGroupDto dto = new NotificationGroupDto();
        when(notificationGroupRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(notificationGroupMapper.fromEntityToNotificationGroupDto(entity)).thenReturn(dto);

        ApiMessageDto<NotificationGroupDto> result = controller.get(1L);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getData()).isSameAs(dto);
        assertThat(result.getMessage()).isEqualTo("Get notification group success");
    }

    @Test
    void shouldThrowNotFoundWhenIdDoesNotExist() {
        when(notificationGroupRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.get(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.NOTIFICATION_GROUP_ERROR_NOT_FOUND);
    }

    @Test
    void shouldCreateNotificationGroupWhenValid() {
        CreateNotificationGroupForm form = new CreateNotificationGroupForm();
        form.setName("Group A");
        form.setClassroomId(2L);

        Classroom classroom = new Classroom();
        classroom.setId(2L);

        NotificationGroup entity = new NotificationGroup();
        NotificationGroupDto idDto = new NotificationGroupDto();

        when(notificationGroupRepository.existsByNameAndClassroomId("Group A", 2L)).thenReturn(false);
        when(classroomRepository.findById(2L)).thenReturn(Optional.of(classroom));
        when(notificationGroupMapper.fromFormToEntity(form)).thenReturn(entity);
        when(notificationGroupMapper.fromEntityToNotificationGroupIdDto(entity)).thenReturn(idDto);

        ApiMessageDto<NotificationGroupDto> result = controller.create(form, null);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getData()).isSameAs(idDto);
        assertThat(result.getMessage()).isEqualTo("Create notification group success");
        assertThat(entity.getClassroom()).isSameAs(classroom);
        verify(notificationGroupRepository).save(entity);
    }

    @Test
    void shouldThrowNotFoundWhenClassroomDoesNotExistOnCreate() {
        CreateNotificationGroupForm form = new CreateNotificationGroupForm();
        form.setName("Group A");
        form.setClassroomId(2L);

        when(notificationGroupRepository.existsByNameAndClassroomId("Group A", 2L)).thenReturn(false);
        when(classroomRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.create(form, null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.CLASSROOM_ERROR_NOT_FOUND);
    }

    @Test
    void shouldThrowBadRequestWhenNameDuplicatedOnCreate() {
        CreateNotificationGroupForm form = new CreateNotificationGroupForm();
        form.setName("Group A");
        form.setClassroomId(2L);

        when(notificationGroupRepository.existsByNameAndClassroomId("Group A", 2L)).thenReturn(true);

        assertThatThrownBy(() -> controller.create(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.NOTIFICATION_GROUP_ERROR_NAME_DUPLICATED);
    }

    @Test
    void shouldUpdateNotificationGroupWhenValid() {
        UpdateNotificationGroupForm form = new UpdateNotificationGroupForm();
        form.setId(1L);
        form.setName("Group B");
        form.setAvatar("avatar-new.png");

        Classroom classroom = new Classroom();
        classroom.setId(2L);

        NotificationGroup entity = new NotificationGroup();
        entity.setId(1L);
        entity.setName("Group A");
        entity.setAvatar("avatar-old.png");
        entity.setClassroom(classroom);

        when(notificationGroupRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(notificationGroupRepository.existsByNameAndClassroomIdAndIdNot("Group B", 2L, 1L)).thenReturn(false);
        doAnswer(invocation -> {
            UpdateNotificationGroupForm f = invocation.getArgument(0);
            NotificationGroup e = invocation.getArgument(1);
            e.setName(f.getName());
            e.setAvatar(f.getAvatar());
            return null;
        }).when(notificationGroupMapper).updateEntityFromForm(any(UpdateNotificationGroupForm.class), any(NotificationGroup.class));

        ApiMessageDto<Void> result = controller.update(form, null);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Update notification group success");
        assertThat(entity.getName()).isEqualTo("Group B");
        assertThat(entity.getClassroom()).isSameAs(classroom);
        verify(notificationGroupRepository).save(entity);
        verify(fileService).deleteFile("avatar-old.png");
    }

    @Test
    void shouldNotReassignClassroomIdOnUpdate() {
        UpdateNotificationGroupForm form = new UpdateNotificationGroupForm();
        form.setId(1L);
        form.setName("Group A");
        form.setAvatar("avatar-old.png");

        Classroom originalClassroom = new Classroom();
        originalClassroom.setId(5L);

        NotificationGroup entity = new NotificationGroup();
        entity.setId(1L);
        entity.setName("Group A");
        entity.setAvatar("avatar-old.png");
        entity.setClassroom(originalClassroom);

        when(notificationGroupRepository.findById(1L)).thenReturn(Optional.of(entity));

        controller.update(form, null);

        assertThat(entity.getClassroom()).isSameAs(originalClassroom);
        assertThat(entity.getClassroom().getId()).isEqualTo(5L);
    }

    @Test
    void shouldThrowBadRequestWhenNameDuplicatedOnUpdate() {
        UpdateNotificationGroupForm form = new UpdateNotificationGroupForm();
        form.setId(1L);
        form.setName("Group B");

        Classroom classroom = new Classroom();
        classroom.setId(2L);

        NotificationGroup entity = new NotificationGroup();
        entity.setId(1L);
        entity.setName("Group A");
        entity.setClassroom(classroom);

        when(notificationGroupRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(notificationGroupRepository.existsByNameAndClassroomIdAndIdNot("Group B", 2L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> controller.update(form, null))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.NOTIFICATION_GROUP_ERROR_NAME_DUPLICATED);
    }

    @Test
    void shouldReturnPagedListWhenFilteredByClassroomAndName() {
        NotificationGroupCriteria criteria = new NotificationGroupCriteria();
        criteria.setClassroomId(2L);
        criteria.setName("Group");

        NotificationGroup entity = new NotificationGroup();
        Page<NotificationGroup> page = new PageImpl<>(Collections.singletonList(entity), pageable, 1);
        List<NotificationGroupDto> dtoList = Collections.singletonList(new NotificationGroupDto());

        when(notificationGroupRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(notificationGroupMapper.fromEntityToNotificationGroupDtoList(anyList())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<NotificationGroupDto>>> result = controller.list(criteria, pageable);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Get list success");
        assertThat(result.getData().getContent()).isSameAs(dtoList);
        assertThat(result.getData().getTotalElements()).isEqualTo(1);
        assertThat(result.getData().getTotalPages()).isEqualTo(1);
    }

    @Test
    void shouldDeleteNotificationGroupAndAvatarFile() {
        NotificationGroup entity = new NotificationGroup();
        entity.setAvatar("avatar.png");
        when(notificationGroupRepository.findById(1L)).thenReturn(Optional.of(entity));

        ApiMessageDto<Void> result = controller.delete(1L);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Delete notification group success");
        InOrder inOrder = inOrder(fileService, notificationGroupRepository);
        inOrder.verify(fileService).deleteFiles(Collections.singletonList("avatar.png"));
        inOrder.verify(notificationGroupRepository).delete(entity);
    }

    @Test
    void shouldThrowNotFoundWhenIdDoesNotExistOnDelete() {
        when(notificationGroupRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.NOTIFICATION_GROUP_ERROR_NOT_FOUND);
    }

    @Test
    void shouldReturnAutoCompleteListScopedToClassroom() {
        NotificationGroupCriteria criteria = new NotificationGroupCriteria();
        criteria.setClassroomId(2L);

        NotificationGroup entity = new NotificationGroup();
        Page<NotificationGroup> page = new PageImpl<>(Collections.singletonList(entity), pageable, 1);
        List<NotificationGroupDto> dtoList = Collections.singletonList(new NotificationGroupDto());

        when(notificationGroupRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(notificationGroupMapper.fromEntityToNotificationGroupDtoAutoCompleteList(anyList())).thenReturn(dtoList);

        ApiMessageDto<ResponseListDto<List<NotificationGroupDto>>> result = controller.autoComplete(criteria, pageable);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Get auto complete notification groups success");
        assertThat(result.getData().getContent()).isSameAs(dtoList);
    }
}
