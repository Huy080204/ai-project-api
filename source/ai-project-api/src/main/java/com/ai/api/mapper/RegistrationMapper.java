package com.ai.api.mapper;

import com.ai.api.dto.registration.RegistrationDto;
import com.ai.api.form.registration.CreateRegistrationForm;
import com.ai.api.model.Registration;
import org.mapstruct.BeanMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {ClassroomMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RegistrationMapper {
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "message", target = "message")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromCreateRegistrationFormToEntity")
    Registration fromCreateRegistrationFormToEntity(CreateRegistrationForm createRegistrationForm);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "classroom", target = "classroom", qualifiedByName = "fromEntityToClassroomDto")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "message", target = "message")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToRegistrationDto")
    RegistrationDto fromEntityToRegistrationDto(Registration registration);

    @IterableMapping(elementTargetType = RegistrationDto.class, qualifiedByName = "fromEntityToRegistrationDto")
    @Named("fromEntityToRegistrationDtoList")
    List<RegistrationDto> fromEntityToRegistrationDtoList(List<Registration> registrations);
}
