package com.ai.api.mapper;

import com.ai.api.dto.mentor.MentorDto;
import com.ai.api.form.mentor.CreateMentorForm;
import com.ai.api.form.mentor.UpdateMentorForm;
import com.ai.api.model.Account;
import com.ai.api.model.Mentor;
import org.mapstruct.BeanMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {AccountMapper.class})
public interface MentorMapper {
    @Mapping(source = "username", target = "username")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "avatarPath", target = "avatarPath")
    @BeanMapping(ignoreByDefault = true)
    Account fromFormToAccount(CreateMentorForm form);

    @Mapping(source = "position", target = "position")
    @Mapping(source = "description", target = "description")
    @BeanMapping(ignoreByDefault = true)
    Mentor fromFormToEntity(CreateMentorForm form);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "account", target = "account", qualifiedByName = "fromAccountToDto")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToMentorDto")
    MentorDto fromEntityToMentorDto(Mentor mentor);

    @IterableMapping(elementTargetType = MentorDto.class, qualifiedByName = "fromEntityToMentorDto")
    @Named("fromEntityToMentorDtoList")
    List<MentorDto> fromEntityToMentorDtoList(List<Mentor> mentors);

    @Mapping(source = "position", target = "position")
    @Mapping(source = "description", target = "description")
    @BeanMapping(ignoreByDefault = true)
    void updateEntityFromForm(UpdateMentorForm form, @MappingTarget Mentor mentor);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "account", target = "account", qualifiedByName = "fromAccountToAutoCompleteDto")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToMentorDtoPublic")
    MentorDto fromEntityToMentorDtoPublic(Mentor mentor);

    @IterableMapping(elementTargetType = MentorDto.class, qualifiedByName = "fromEntityToMentorDtoPublic")
    @Named("fromEntityToMentorDtoPublicList")
    List<MentorDto> fromEntityToMentorDtoPublicList(List<Mentor> mentors);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "account", target = "account", qualifiedByName = "fromAccountToAutoCompleteDto")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToMentorAutoCompleteDto")
    MentorDto fromEntityToMentorAutoCompleteDto(Mentor mentor);

    @IterableMapping(elementTargetType = MentorDto.class, qualifiedByName = "fromEntityToMentorAutoCompleteDto")
    @Named("fromEntityToMentorAutoCompleteDtoList")
    List<MentorDto> fromEntityToMentorAutoCompleteDtoList(List<Mentor> mentors);
}
