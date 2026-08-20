package com.ai.api.controller;

import com.ai.api.dto.ApiMessageDto;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.validation.BindingResult;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link NewsController}.
 */
@ExtendWith(MockitoExtension.class)
class NewsControllerTest {

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private NewsMapper newsMapper;

    @Mock
    private FileService fileService;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private NewsController newsController;

    // ------------------------------------------------------------------ create

    @Test
    void shouldCreateNewsSuccessfully() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        CreateNewsForm form = new CreateNewsForm();
        form.setTitle("Title One");
        form.setCategoryId(5L);

        Category category = new Category();
        category.setId(5L);

        News news = new News();

        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(newsMapper.fromCreateNewsFormToEntity(form)).thenReturn(news);

        // Act
        ApiMessageDto<NewsDto> result = newsController.create(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(news.getCategory()).isEqualTo(category);
        verify(newsRepository).save(news);
    }

    @Test
    void shouldThrowNotFoundWhenCreateCategoryUnknown() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        CreateNewsForm form = new CreateNewsForm();
        form.setTitle("Title One");
        form.setCategoryId(99L);

        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> newsController.create(form, bindingResult))
                .isInstanceOf(NotFoundException.class);
        verify(newsRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ update

    @Test
    void shouldUpdateNewsSuccessfully() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateNewsForm form = new UpdateNewsForm();
        form.setId(1L);
        form.setTitle("Title One");
        form.setCategoryId(2L);
        form.setAvatar("/same.png");

        News news = new News();
        news.setId(1L);
        news.setAvatar("/same.png");

        Category category = new Category();
        category.setId(2L);

        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));

        // Act
        ApiMessageDto<Void> result = newsController.update(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(news.getCategory()).isEqualTo(category);
        verify(newsRepository).save(news);
    }

    @Test
    void shouldThrowNotFoundWhenUpdateCategoryUnknown() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateNewsForm form = new UpdateNewsForm();
        form.setId(1L);
        form.setTitle("Title One");
        form.setCategoryId(99L);

        News news = new News();
        news.setId(1L);

        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> newsController.update(form, bindingResult))
                .isInstanceOf(NotFoundException.class);
        verify(newsRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenUpdateNewsIdMissing() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateNewsForm form = new UpdateNewsForm();
        form.setId(1L);
        form.setTitle("Title One");
        form.setCategoryId(2L);

        when(newsRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> newsController.update(form, bindingResult))
                .isInstanceOf(NotFoundException.class);
        verify(newsRepository, never()).save(any());
    }

    @Test
    void shouldDeleteOldAvatarWhenUpdateAvatarChanged() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateNewsForm form = new UpdateNewsForm();
        form.setId(1L);
        form.setTitle("Title One");
        form.setCategoryId(2L);
        form.setAvatar("/new.png");

        News news = new News();
        news.setId(1L);
        news.setAvatar("/old.png");

        Category category = new Category();
        category.setId(2L);

        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));

        // Act
        newsController.update(form, bindingResult);

        // Assert
        verify(fileService).deleteFile("/old.png");
    }

    @Test
    void shouldNotDeleteAvatarWhenUpdateAvatarUnchanged() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateNewsForm form = new UpdateNewsForm();
        form.setId(1L);
        form.setTitle("Title One");
        form.setCategoryId(2L);
        form.setAvatar("/same.png");

        News news = new News();
        news.setId(1L);
        news.setAvatar("/same.png");

        Category category = new Category();
        category.setId(2L);

        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));

        // Act
        newsController.update(form, bindingResult);

        // Assert
        verify(fileService, never()).deleteFile(anyString());
    }

    // --------------------------------------------------------------------- get

    @Test
    void shouldThrowNotFoundWhenGetIdMissing() {
        // Arrange
        when(newsRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> newsController.get(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldGetNewsSuccessfully() {
        // Arrange
        News news = new News();
        news.setId(1L);
        news.setTitle("Title One");

        NewsDto dto = new NewsDto();
        dto.setId(1L);
        dto.setTitle("Title One");

        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));
        when(newsMapper.fromEntityToNewsDto(news)).thenReturn(dto);

        // Act
        ApiMessageDto<NewsDto> result = newsController.get(1L);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData()).isEqualTo(dto);
    }

    // -------------------------------------------------------------------- list

    @Test
    @SuppressWarnings("unchecked")
    void shouldListNewsSuccessfully() {
        // Arrange
        NewsCriteria criteria = new NewsCriteria();
        criteria.setTitle("Title");
        Pageable pageable = PageRequest.of(0, 10);

        News news = new News();
        news.setId(1L);
        news.setTitle("Title One");
        Page<News> page = new PageImpl<>(Collections.singletonList(news), pageable, 1);

        NewsDto dto = new NewsDto();
        dto.setId(1L);
        dto.setTitle("Title One");

        when(newsRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(newsMapper.fromEntityToNewsDtoList(anyList())).thenReturn(Collections.singletonList(dto));

        // Act
        ApiMessageDto<ResponseListDto<List<NewsDto>>> result = newsController.list(criteria, pageable);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).hasSize(1);
        assertThat(result.getData().getContent().get(0).getTitle()).isEqualTo("Title One");
        verify(newsRepository).findAll(any(Specification.class), eq(pageable));
    }

    // ------------------------------------------------------------------ delete

    @Test
    void shouldThrowNotFoundWhenDeleteIdMissing() {
        // Arrange
        when(newsRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> newsController.delete(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeleteAvatarFileWhenDeletingNewsWithNonBlankAvatar() {
        // Arrange
        News news = new News();
        news.setId(1L);
        news.setAvatar("/to-delete.png");

        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));

        // Act
        ApiMessageDto<Void> result = newsController.delete(1L);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(fileService).deleteFile("/to-delete.png");
        verify(newsRepository).deleteById(1L);
    }

    @Test
    void shouldNotDeleteFileWhenDeletingNewsWithBlankAvatar() {
        // Arrange
        News news = new News();
        news.setId(1L);
        news.setAvatar("");

        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));

        // Act
        ApiMessageDto<Void> result = newsController.delete(1L);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(fileService, never()).deleteFile(anyString());
        verify(newsRepository).deleteById(1L);
    }
}
