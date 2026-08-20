package com.ai.api.mapper;

import com.ai.api.dto.category.CategoryDto;
import com.ai.api.dto.category.CategoryTreeDto;
import com.ai.api.form.category.CreateCategoryForm;
import com.ai.api.form.category.UpdateCategoryForm;
import com.ai.api.model.Category;
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
public interface CategoryMapper {
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "kind", target = "kind")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromCreateCategoryFormToEntity")
    Category fromCreateCategoryFormToEntity(CreateCategoryForm createCategoryForm);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "kind", target = "kind")
    @BeanMapping(ignoreByDefault = true)
    void updateEntityFromForm(UpdateCategoryForm updateCategoryForm, @MappingTarget Category category);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "ordering", target = "ordering")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToCategoryDto")
    CategoryDto fromEntityToCategoryDto(Category category);

    @IterableMapping(elementTargetType = CategoryDto.class, qualifiedByName = "fromEntityToCategoryDto")
    @Named("fromEntityToCategoryDtoList")
    List<CategoryDto> fromEntityToCategoryDtoList(List<Category> categories);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "kind", target = "kind")
    @Mapping(source = "ordering", target = "ordering")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToCategoryTreeDto")
    CategoryTreeDto fromEntityToCategoryTreeDto(Category category);

    @IterableMapping(elementTargetType = CategoryTreeDto.class, qualifiedByName = "fromEntityToCategoryTreeDto")
    @Named("fromEntityToCategoryTreeDtoList")
    List<CategoryTreeDto> fromEntityToCategoryTreeDtoList(List<Category> categories);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToCategoryAutoCompleteDto")
    CategoryDto fromEntityToCategoryAutoCompleteDto(Category category);

    @IterableMapping(elementTargetType = CategoryDto.class, qualifiedByName = "fromEntityToCategoryAutoCompleteDto")
    @Named("fromEntityToCategoryAutoCompleteDtoList")
    List<CategoryDto> fromEntityToCategoryAutoCompleteDtoList(List<Category> categories);

    @Mapping(source = "id", target = "id")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToCategoryIdDto")
    CategoryDto fromEntityToCategoryIdDto(Category category);
}
