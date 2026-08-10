package com.ai.api.mapper;

import com.ai.api.dto.reaction.ReactionDto;
import com.ai.api.form.reaction.CreateReactionForm;
import com.ai.api.form.reaction.UpdateReactionForm;
import com.ai.api.model.Reaction;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {CourseMapper.class, StudentMapper.class})
public interface ReactionMapper {
    @Mapping(source = "course", target = "course", qualifiedByName = "fromEntityToCourseDto")
    @Mapping(source = "student", target = "student", qualifiedByName = "fromEntityToStudentDto")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "type", target = "type")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToReactionDto")
    ReactionDto fromEntityToReactionDto(Reaction reaction);

    @IterableMapping(elementTargetType = ReactionDto.class, qualifiedByName = "fromEntityToReactionDto")
    @Named("fromEntityToReactionDtoList")
    List<ReactionDto> fromEntityToReactionDtoList(List<Reaction> reactions);

    @Mapping(source = "course", target = "course", qualifiedByName = "fromEntityToCourseDtoAutoComplete")
    @Mapping(source = "student", target = "student", qualifiedByName = "fromEntityToStudentAutoCompleteDto")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "type", target = "type")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToReactionDtoPublic")
    ReactionDto fromEntityToReactionDtoPublic(Reaction reaction);

    @IterableMapping(elementTargetType = ReactionDto.class, qualifiedByName = "fromEntityToReactionDtoPublic")
    @Named("fromEntityToReactionDtoPublicList")
    List<ReactionDto> fromEntityToReactionDtoPublicList(List<Reaction> reactions);

    @Mapping(source = "content", target = "content")
    @Mapping(source = "type", target = "type")
    @BeanMapping(ignoreByDefault = true)
    Reaction fromFormToEntity(CreateReactionForm createReactionForm);

    @Mapping(source = "content", target = "content")
    @Mapping(source = "type", target = "type")
    @BeanMapping(ignoreByDefault = true)
    void updateEntityFromForm(UpdateReactionForm updateReactionForm, @MappingTarget Reaction reaction);
}
