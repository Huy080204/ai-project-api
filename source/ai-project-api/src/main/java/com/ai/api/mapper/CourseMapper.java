package com.ai.api.mapper;

import com.ai.api.dto.course.CourseDto;
import com.ai.api.form.course.CreateCourseForm;
import com.ai.api.form.course.UpdateCourseForm;
import com.ai.api.model.Course;
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
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CourseMapper {
    @Mapping(source = "name", target = "name")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "shortDescription", target = "shortDescription")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromCreateCourseFormToEntity")
    Course fromCreateCourseFormToEntity(CreateCourseForm createCourseForm);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "shortDescription", target = "shortDescription")
    @BeanMapping(ignoreByDefault = true)
    void updateEntityFromForm(UpdateCourseForm updateCourseForm, @MappingTarget Course course);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "shortDescription", target = "shortDescription")
    @Mapping(source = "totalTimeline", target = "totalTimeline")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToCourseDto")
    CourseDto fromEntityToCourseDto(Course course);

    @IterableMapping(elementTargetType = CourseDto.class, qualifiedByName = "fromEntityToCourseDto")
    @Named("fromEntityToCourseDtoList")
    List<CourseDto> fromEntityToCourseDtoList(List<Course> courses);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "avatar", target = "avatar")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToCourseDtoAutoComplete")
    CourseDto fromEntityToCourseDtoAutoComplete(Course course);

    @IterableMapping(elementTargetType = CourseDto.class, qualifiedByName = "fromEntityToCourseDtoAutoComplete")
    @Named("fromEntityToCourseDtoAutoCompleteList")
    List<CourseDto> fromEntityToCourseDtoAutoCompleteList(List<Course> courses);

    @Mapping(source = "id", target = "id")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToCourseIdDto")
    CourseDto fromEntityToCourseIdDto(Course course);
}
