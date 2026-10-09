package com.ai.api.mapper;

import com.ai.api.dto.tag.TagDto;
import com.ai.api.form.tag.CreateTagForm;
import com.ai.api.form.tag.UpdateTagForm;
import com.ai.api.model.Tag;
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
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TagMapper {

    @Mapping(source = "name", target = "name")
    @Mapping(source = "colorCode", target = "colorCode")
    @BeanMapping(ignoreByDefault = true)
    Tag fromCreateFormToEntity(CreateTagForm createTagForm);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "colorCode", target = "colorCode")
    @BeanMapping(ignoreByDefault = true)
    void fromUpdateFormToEntity(UpdateTagForm updateTagForm, @MappingTarget Tag tag);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "colorCode", target = "colorCode")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToTagDto")
    TagDto fromEntityToTagDto(Tag tag);

    @IterableMapping(elementTargetType = TagDto.class, qualifiedByName = "fromEntityToTagDto")
    List<TagDto> fromEntityListToTagDtoList(List<Tag> tags);
}
