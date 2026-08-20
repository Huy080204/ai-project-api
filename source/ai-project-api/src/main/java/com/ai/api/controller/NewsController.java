package com.ai.api.controller;

import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.news.NewsDto;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.news.CreateNewsForm;
import com.ai.api.form.news.UpdateNewsForm;
import com.ai.api.mapper.NewsMapper;
import com.ai.api.model.Category;
import com.ai.api.model.News;
import com.ai.api.model.criteria.NewsCriteria;
import com.ai.api.repository.CategoryRepository;
import com.ai.api.repository.NewsRepository;
import com.ai.api.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/v1/news")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class NewsController extends ABasicController {
    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private NewsMapper newsMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NEW_C')")
    public ApiMessageDto<NewsDto> create(@Valid @RequestBody CreateNewsForm createNewsForm, BindingResult bindingResult) {
        Category category = categoryRepository.findById(createNewsForm.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found", ErrorCode.NEWS_ERROR_CATEGORY_NOT_FOUND));
        News news = newsMapper.fromCreateNewsFormToEntity(createNewsForm);
        news.setCategory(category);
        newsRepository.save(news);
        return makeSuccessResponse(newsMapper.fromEntityToNewsIdDto(news), "Create news success");
    }

    @Transactional
    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NEW_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateNewsForm updateNewsForm, BindingResult bindingResult) {
        News news = newsRepository.findById(updateNewsForm.getId())
                .orElseThrow(() -> new NotFoundException("News not found", ErrorCode.NEWS_ERROR_NOT_FOUND));
        Category category = categoryRepository.findById(updateNewsForm.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found", ErrorCode.NEWS_ERROR_CATEGORY_NOT_FOUND));
        String oldAvatar = news.getAvatar();
        if (StringUtils.isNoneBlank(oldAvatar) && !Objects.equals(updateNewsForm.getAvatar(), oldAvatar)) {
            fileService.deleteFile(oldAvatar);
        }
        newsMapper.updateEntityFromForm(updateNewsForm, news);
        news.setCategory(category);
        newsRepository.save(news);
        return makeSuccessResponse("Update news success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NEW_V')")
    public ApiMessageDto<NewsDto> get(@PathVariable("id") Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("News not found", ErrorCode.NEWS_ERROR_NOT_FOUND));
        return makeSuccessResponse(newsMapper.fromEntityToNewsDto(news), "Get news success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NEW_L')")
    public ApiMessageDto<ResponseListDto<List<NewsDto>>> list(NewsCriteria criteria, Pageable pageable) {
        Page<News> newsPage = newsRepository.findAll(criteria.getCriteria(), pageable);
        return makeSuccessResponse(makeResponseListDto(newsPage, newsMapper::fromEntityToNewsDtoList), "List news success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('NEW_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("News not found", ErrorCode.NEWS_ERROR_NOT_FOUND));
        if (StringUtils.isNoneBlank(news.getAvatar())) {
            fileService.deleteFile(news.getAvatar());
        }
        newsRepository.deleteById(id);
        return makeSuccessResponse("Delete news success");
    }
}
