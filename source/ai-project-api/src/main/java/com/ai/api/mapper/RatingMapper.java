package com.ai.api.mapper;

import com.ai.api.dto.rating.RatingDto;
import com.ai.api.form.rating.CreateRatingForm;
import com.ai.api.form.rating.UpdateRatingForm;
import com.ai.api.model.Rating;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {CourseMapper.class, StudentMapper.class})
public interface RatingMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "message", target = "message")
    @Mapping(source = "star", target = "star")
    Rating fromFormToEntity(CreateRatingForm form);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "message", target = "message")
    @Mapping(source = "star", target = "star")
    void updateEntityFromForm(UpdateRatingForm form, @MappingTarget Rating rating);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "id", target = "id")
    @Mapping(source = "course", target = "course", qualifiedByName = "fromEntityToCourseDto")
    @Mapping(source = "student", target = "student", qualifiedByName = "fromEntityToStudentDto")
    @Mapping(source = "message", target = "message")
    @Mapping(source = "star", target = "star")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "status", target = "status")
    @Named("fromEntityToRatingDto")
    RatingDto fromEntityToRatingDto(Rating rating);

    @IterableMapping(elementTargetType = RatingDto.class, qualifiedByName = "fromEntityToRatingDto")
    @Named("fromEntityToRatingDtoList")
    List<RatingDto> fromEntityToRatingDtoList(List<Rating> ratings);

    // Public/unauthenticated shape: status/createdDate/modifiedDate deliberately left
    // unmapped (null) — mirrors CompanyMapper.fromEntityToCompanyDtoPublic(List). Do not add them.
    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "id", target = "id")
    @Mapping(source = "course", target = "course", qualifiedByName = "fromEntityToCourseDto")
    @Mapping(source = "student", target = "student", qualifiedByName = "fromEntityToStudentDto")
    @Mapping(source = "message", target = "message")
    @Mapping(source = "star", target = "star")
    @Named("fromEntityToRatingDtoPublic")
    RatingDto fromEntityToRatingDtoPublic(Rating rating);

    @IterableMapping(elementTargetType = RatingDto.class, qualifiedByName = "fromEntityToRatingDtoPublic")
    @Named("fromEntityToRatingDtoPublicList")
    List<RatingDto> fromEntityToRatingDtoPublicList(List<Rating> ratings);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "id", target = "id")
    @Named("fromEntityToRatingIdDto")
    RatingDto fromEntityToRatingIdDto(Rating rating);
}
