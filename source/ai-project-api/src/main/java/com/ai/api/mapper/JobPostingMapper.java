package com.ai.api.mapper;

import com.ai.api.dto.jobposting.JobPostingDto;
import com.ai.api.form.jobposting.CreateJobPostingForm;
import com.ai.api.form.jobposting.UpdateJobPostingForm;
import com.ai.api.model.JobPosting;
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
public interface JobPostingMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "position", target = "position")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "salary", target = "salary")
    JobPosting fromFormToEntity(CreateJobPostingForm form);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "position", target = "position")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "salary", target = "salary")
    void updateEntityFromForm(UpdateJobPostingForm form, @MappingTarget JobPosting jobPosting);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "id", target = "id")
    @Mapping(source = "position", target = "position")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "salary", target = "salary")
    @Mapping(source = "state", target = "state")
    @Mapping(source = "company.id", target = "companyId")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "status", target = "status")
    @Named("fromEntityToJobPostingDto")
    JobPostingDto fromEntityToJobPostingDto(JobPosting jobPosting);

    @IterableMapping(elementTargetType = JobPostingDto.class, qualifiedByName = "fromEntityToJobPostingDto")
    @Named("fromEntityToJobPostingDtoList")
    List<JobPostingDto> fromEntityToJobPostingDtoList(List<JobPosting> jobPostings);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "id", target = "id")
    @Named("fromEntityToJobPostingIdDto")
    JobPostingDto fromEntityToJobPostingIdDto(JobPosting jobPosting);
}
