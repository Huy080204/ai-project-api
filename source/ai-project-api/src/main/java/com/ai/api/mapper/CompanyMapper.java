package com.ai.api.mapper;

import com.ai.api.dto.company.CompanyDto;
import com.ai.api.form.company.CreateCompanyForm;
import com.ai.api.form.company.UpdateCompanyForm;
import com.ai.api.model.Company;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompanyMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "name", target = "name")
    @Mapping(source = "avatar", target = "avatar")
    Company fromFormToEntity(CreateCompanyForm form);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "name", target = "name")
    @Mapping(source = "avatar", target = "avatar")
    void updateEntityFromForm(UpdateCompanyForm form, @MappingTarget Company company);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "status", target = "status")
    @Named("fromEntityToCompanyDto")
    CompanyDto fromEntityToCompanyDto(Company company);

    @IterableMapping(elementTargetType = CompanyDto.class, qualifiedByName = "fromEntityToCompanyDto")
    @Named("fromEntityToCompanyDtoList")
    List<CompanyDto> fromEntityToCompanyDtoList(List<Company> companies);

    // Public/unauthenticated shape: status/createdDate/modifiedDate are deliberately left
    // unmapped (null) — do not add them here.
    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "avatar", target = "avatar")
    @Named("fromEntityToCompanyDtoPublic")
    CompanyDto fromEntityToCompanyDtoPublic(Company company);

    @IterableMapping(elementTargetType = CompanyDto.class, qualifiedByName = "fromEntityToCompanyDtoPublic")
    @Named("fromEntityToCompanyDtoPublicList")
    List<CompanyDto> fromEntityToCompanyDtoPublicList(List<Company> companies);
}
