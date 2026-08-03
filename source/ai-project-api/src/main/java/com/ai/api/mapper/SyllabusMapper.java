package com.ai.api.mapper;

import com.ai.api.dto.syllabus.SyllabusDto;
import com.ai.api.form.syllabus.CreateSyllabusForm;
import com.ai.api.form.syllabus.UpdateSyllabusForm;
import com.ai.api.model.Syllabus;
import org.mapstruct.BeanMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {CourseMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SyllabusMapper {
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "avatar", target = "avatar", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(source = "description", target = "description")
    @Mapping(source = "ordering", target = "ordering")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromCreateSyllabusFormToEntity")
    Syllabus fromCreateSyllabusFormToEntity(CreateSyllabusForm createSyllabusForm);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "avatar", target = "avatar", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(source = "description", target = "description")
    @Mapping(source = "ordering", target = "ordering")
    @BeanMapping(ignoreByDefault = true)
    void updateEntityFromForm(UpdateSyllabusForm updateSyllabusForm, @MappingTarget Syllabus syllabus);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "course", target = "course", qualifiedByName = "fromEntityToCourseDto")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "ordering", target = "ordering")
    @Mapping(source = "timeline", target = "timeline")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToSyllabusDto")
    SyllabusDto fromEntityToSyllabusDto(Syllabus syllabus);

    @IterableMapping(elementTargetType = SyllabusDto.class, qualifiedByName = "fromEntityToSyllabusDto")
    @Named("fromEntityToSyllabusDtoList")
    List<SyllabusDto> fromEntityToSyllabusDtoList(List<Syllabus> syllabuses);
}
