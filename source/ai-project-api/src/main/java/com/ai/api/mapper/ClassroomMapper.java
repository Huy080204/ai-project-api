package com.ai.api.mapper;

import com.ai.api.dto.classroom.ClassroomDto;
import com.ai.api.form.classroom.CreateClassroomForm;
import com.ai.api.form.classroom.UpdateClassroomForm;
import com.ai.api.model.Classroom;
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
public interface ClassroomMapper {
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "price", target = "price")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromCreateClassroomFormToEntity")
    Classroom fromCreateClassroomFormToEntity(CreateClassroomForm createClassroomForm);

    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "price", target = "price")
    @BeanMapping(ignoreByDefault = true)
    void updateEntityFromForm(UpdateClassroomForm updateClassroomForm, @MappingTarget Classroom classroom);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "course", target = "course", qualifiedByName = "fromEntityToCourseDto")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "state", target = "state")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToClassroomDto")
    ClassroomDto fromEntityToClassroomDto(Classroom classroom);

    @IterableMapping(elementTargetType = ClassroomDto.class, qualifiedByName = "fromEntityToClassroomDto")
    @Named("fromEntityToClassroomDtoList")
    List<ClassroomDto> fromEntityToClassroomDtoList(List<Classroom> classrooms);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "course", target = "course", qualifiedByName = "fromEntityToCourseDtoAutoComplete")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToClassroomDtoAutoComplete")
    ClassroomDto fromEntityToClassroomDtoAutoComplete(Classroom classroom);

    @IterableMapping(elementTargetType = ClassroomDto.class, qualifiedByName = "fromEntityToClassroomDtoAutoComplete")
    @Named("fromEntityToClassroomDtoAutoCompleteList")
    List<ClassroomDto> fromEntityToClassroomDtoAutoCompleteList(List<Classroom> classrooms);
}
