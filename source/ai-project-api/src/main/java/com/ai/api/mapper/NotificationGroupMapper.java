package com.ai.api.mapper;

import com.ai.api.dto.notificationgroup.NotificationGroupDto;
import com.ai.api.form.notificationgroup.CreateNotificationGroupForm;
import com.ai.api.form.notificationgroup.UpdateNotificationGroupForm;
import com.ai.api.model.NotificationGroup;
import org.mapstruct.BeanMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {ClassroomMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationGroupMapper {
    @Mapping(source = "name", target = "name")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "zaloUrl", target = "zaloUrl")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromFormToEntity")
    NotificationGroup fromFormToEntity(CreateNotificationGroupForm createNotificationGroupForm);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "zaloUrl", target = "zaloUrl")
    @BeanMapping(ignoreByDefault = true)
    @Named("updateEntityFromForm")
    void updateEntityFromForm(UpdateNotificationGroupForm updateNotificationGroupForm, @MappingTarget NotificationGroup notificationGroup);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "classroom", target = "classroom", qualifiedByName = "fromEntityToClassroomDto")
    @Mapping(source = "name", target = "groupName")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "zaloUrl", target = "zaloUrl")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToNotificationGroupDto")
    NotificationGroupDto fromEntityToNotificationGroupDto(NotificationGroup notificationGroup);

    @IterableMapping(elementTargetType = NotificationGroupDto.class, qualifiedByName = "fromEntityToNotificationGroupDto")
    @Named("fromEntityToNotificationGroupDtoList")
    List<NotificationGroupDto> fromEntityToNotificationGroupDtoList(List<NotificationGroup> notificationGroups);

    @Mapping(source = "id", target = "id")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToNotificationGroupIdDto")
    NotificationGroupDto fromEntityToNotificationGroupIdDto(NotificationGroup notificationGroup);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "groupName")
    @Mapping(source = "avatar", target = "avatar")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToNotificationGroupDtoAutoComplete")
    NotificationGroupDto fromEntityToNotificationGroupDtoAutoComplete(NotificationGroup notificationGroup);

    @IterableMapping(elementTargetType = NotificationGroupDto.class, qualifiedByName = "fromEntityToNotificationGroupDtoAutoComplete")
    @Named("fromEntityToNotificationGroupDtoAutoCompleteList")
    List<NotificationGroupDto> fromEntityToNotificationGroupDtoAutoCompleteList(List<NotificationGroup> notificationGroups);
}
