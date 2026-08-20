package com.ai.api.mapper;

import com.ai.api.dto.submission.SubmissionDto;
import com.ai.api.form.submission.CreateSubmissionForm;
import com.ai.api.form.submission.GradeSubmissionForm;
import com.ai.api.form.submission.UpdateSubmissionForm;
import com.ai.api.model.Submission;
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
public interface SubmissionMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "assignment.id", target = "assignmentId")
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "fileUrl", target = "fileUrl")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "feedback", target = "feedback")
    @Mapping(source = "score", target = "score")
    @Mapping(source = "state", target = "state")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "createdDate", target = "createdDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToSubmissionDto")
    SubmissionDto fromEntityToSubmissionDto(Submission submission);

    @IterableMapping(elementTargetType = SubmissionDto.class, qualifiedByName = "fromEntityToSubmissionDto")
    List<SubmissionDto> fromEntityToSubmissionDtoList(List<Submission> submissions);

    @Mapping(source = "fileUrl", target = "fileUrl")
    @Mapping(source = "content", target = "content")
    @BeanMapping(ignoreByDefault = true)
    Submission fromFormToEntity(CreateSubmissionForm createSubmissionForm);

    @Mapping(source = "fileUrl", target = "fileUrl")
    @Mapping(source = "content", target = "content")
    @BeanMapping(ignoreByDefault = true)
    void updateEntityFromForm(UpdateSubmissionForm updateSubmissionForm, @MappingTarget Submission submission);

    @Mapping(source = "score", target = "score")
    @Mapping(source = "feedback", target = "feedback")
    @BeanMapping(ignoreByDefault = true)
    void updateEntityFromGradeForm(GradeSubmissionForm gradeSubmissionForm, @MappingTarget Submission submission);

    @Mapping(source = "id", target = "id")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToSubmissionIdDto")
    SubmissionDto fromEntityToSubmissionIdDto(Submission submission);
}
