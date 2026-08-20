package com.ai.api.mapper;

import com.ai.api.dto.assignment.AssignmentDto;
import com.ai.api.form.assignment.CreateAssignmentForm;
import com.ai.api.form.assignment.UpdateAssignmentForm;
import com.ai.api.model.Assignment;
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
public interface AssignmentMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "syllabus.id", target = "syllabusId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "deadline", target = "deadline")
    @Mapping(source = "fileAttachmentUrl", target = "fileAttachmentUrl")
    @Mapping(source = "state", target = "state")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToAssignmentDto")
    AssignmentDto fromEntityToAssignmentDto(Assignment assignment);

    @IterableMapping(elementTargetType = AssignmentDto.class, qualifiedByName = "fromEntityToAssignmentDto")
    List<AssignmentDto> fromEntityToAssignmentDtoList(List<Assignment> assignments);

    @Mapping(source = "id", target = "id")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToAssignmentIdDto")
    AssignmentDto fromEntityToAssignmentIdDto(Assignment assignment);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "deadline", target = "deadline")
    @Mapping(source = "fileAttachmentUrl", target = "fileAttachmentUrl")
    @BeanMapping(ignoreByDefault = true)
    Assignment fromFormToEntity(CreateAssignmentForm createAssignmentForm);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "deadline", target = "deadline")
    @Mapping(source = "fileAttachmentUrl", target = "fileAttachmentUrl")
    @Mapping(source = "state", target = "state")
    @BeanMapping(ignoreByDefault = true)
    void updateEntityFromForm(UpdateAssignmentForm updateAssignmentForm, @MappingTarget Assignment assignment);
}
