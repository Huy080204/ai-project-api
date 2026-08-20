package com.ai.api.mapper;

import com.ai.api.dto.news.NewsDto;
import com.ai.api.form.news.CreateNewsForm;
import com.ai.api.form.news.UpdateNewsForm;
import com.ai.api.model.News;
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
public interface NewsMapper {
    @Mapping(source = "title", target = "title")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "shortDescription", target = "shortDescription")
    @Mapping(source = "content", target = "content")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromCreateNewsFormToEntity")
    News fromCreateNewsFormToEntity(CreateNewsForm createNewsForm);

    @Mapping(source = "title", target = "title")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "shortDescription", target = "shortDescription")
    @Mapping(source = "content", target = "content")
    @BeanMapping(ignoreByDefault = true)
    void updateEntityFromForm(UpdateNewsForm updateNewsForm, @MappingTarget News news);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "avatar", target = "avatar")
    @Mapping(source = "shortDescription", target = "shortDescription")
    @Mapping(source = "content", target = "content")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @Mapping(source = "status", target = "status")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToNewsDto")
    NewsDto fromEntityToNewsDto(News news);

    @IterableMapping(elementTargetType = NewsDto.class, qualifiedByName = "fromEntityToNewsDto")
    @Named("fromEntityToNewsDtoList")
    List<NewsDto> fromEntityToNewsDtoList(List<News> newsList);

    @Mapping(source = "id", target = "id")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToNewsIdDto")
    NewsDto fromEntityToNewsIdDto(News news);
}
