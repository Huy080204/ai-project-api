package com.ai.api.mapper;

import com.ai.api.dto.syllabusmaterial.SyllabusMaterialDto;
import com.ai.api.form.syllabusmaterial.CreateSyllabusMaterialForm;
import com.ai.api.form.syllabusmaterial.UpdateSyllabusMaterialForm;
import com.ai.api.model.SyllabusMaterial;
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
public interface SyllabusMaterialMapper {

    @Mapping(source = "title", target = "title")
    @Mapping(source = "fileUrl", target = "fileUrl")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromCreateSyllabusMaterialFormToEntity")
    SyllabusMaterial fromCreateSyllabusMaterialFormToEntity(CreateSyllabusMaterialForm createSyllabusMaterialForm);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "fileUrl", target = "fileUrl")
    @BeanMapping(ignoreByDefault = true)
    @Named("updateEntityFromForm")
    void updateEntityFromForm(UpdateSyllabusMaterialForm updateSyllabusMaterialForm, @MappingTarget SyllabusMaterial syllabusMaterial);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "syllabus.id", target = "syllabusId")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "fileUrl", target = "fileUrl")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToSyllabusMaterialDto")
    SyllabusMaterialDto fromEntityToSyllabusMaterialDto(SyllabusMaterial syllabusMaterial);

    @IterableMapping(elementTargetType = SyllabusMaterialDto.class, qualifiedByName = "fromEntityToSyllabusMaterialDto")
    List<SyllabusMaterialDto> fromEntityToSyllabusMaterialDtoList(List<SyllabusMaterial> syllabusMaterials);
}
