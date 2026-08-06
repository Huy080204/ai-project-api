package com.ai.api.controller;

import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.category.CategoryDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.category.CreateCategoryForm;
import com.ai.api.form.category.UpdateCategoryForm;
import com.ai.api.mapper.CategoryMapper;
import com.ai.api.model.Category;
import com.ai.api.model.criteria.CategoryCriteria;
import com.ai.api.repository.CategoryRepository;
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
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link CategoryController}. CategoryController does not exist yet (batch B/T4) —
 * this test is written against its intended API and is expected to fail to compile until then.
 */
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private FileService fileService;

    @InjectMocks
    private CategoryController categoryController;

    // ------------------------------------------------------------------ create

    @Test
    void shouldThrowBadRequestWhenCreateNameAlreadyExists() {
        // Arrange (FR-002)
        BindingResult bindingResult = mock(BindingResult.class);
        CreateCategoryForm form = new CreateCategoryForm();
        form.setName("Category One");

        when(categoryRepository.existsByNameIgnoreCase(anyString())).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> categoryController.create(form, bindingResult))
                .isInstanceOf(BadRequestException.class);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenCreateParentIdUnknown() {
        // Arrange (FR-005)
        BindingResult bindingResult = mock(BindingResult.class);
        CreateCategoryForm form = new CreateCategoryForm();
        form.setName("Category One");
        form.setParentId(99L);

        when(categoryRepository.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> categoryController.create(form, bindingResult))
                .isInstanceOf(NotFoundException.class);
        verify(categoryRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ update

    @Test
    void shouldThrowNotFoundWhenUpdateIdMissing() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateCategoryForm form = new UpdateCategoryForm();
        form.setId(1L);
        form.setName("Category One");

        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> categoryController.update(form, bindingResult))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowBadRequestWhenUpdateNameExistsExcludingSelf() {
        // Arrange (FR-003)
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateCategoryForm form = new UpdateCategoryForm();
        form.setId(1L);
        form.setName("Category Two");

        Category category = new Category();
        category.setId(1L);
        category.setName("Category One");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot(anyString(), any())).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> categoryController.update(form, bindingResult))
                .isInstanceOf(BadRequestException.class);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenUpdateParentIdUnknown() {
        // Arrange (FR-005)
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateCategoryForm form = new UpdateCategoryForm();
        form.setId(1L);
        form.setName("Category One");
        form.setParentId(99L);

        Category category = new Category();
        category.setId(1L);
        category.setName("Category One");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot(anyString(), any())).thenReturn(false);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> categoryController.update(form, bindingResult))
                .isInstanceOf(NotFoundException.class);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenUpdateParentIdEqualsSelf() {
        // Arrange (FR-006)
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateCategoryForm form = new UpdateCategoryForm();
        form.setId(1L);
        form.setName("Category One");
        form.setParentId(1L);

        Category category = new Category();
        category.setId(1L);
        category.setName("Category One");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot(anyString(), any())).thenReturn(false);

        // Act + Assert
        assertThatThrownBy(() -> categoryController.update(form, bindingResult))
                .isInstanceOf(BadRequestException.class);
        verify(categoryRepository, never()).save(any());
    }

    // -------------------------------------------------------------------- list

    @Test
    @SuppressWarnings("unchecked")
    void shouldListCategoriesWithNameAndParentIdFilter() {
        // Arrange (FR-009)
        CategoryCriteria criteria = new CategoryCriteria();
        criteria.setName("Book");
        criteria.setParentId(5L);
        Pageable pageable = PageRequest.of(0, 10);

        Category category = new Category();
        category.setId(1L);
        category.setName("Books");
        Page<Category> page = new PageImpl<>(Collections.singletonList(category), pageable, 1);

        CategoryDto dto = new CategoryDto();
        dto.setId(1L);
        dto.setName("Books");

        when(categoryRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(categoryMapper.fromEntityToCategoryDtoList(anyList())).thenReturn(Collections.singletonList(dto));

        // Act
        ApiMessageDto<ResponseListDto<List<CategoryDto>>> result = categoryController.list(criteria, pageable);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).hasSize(1);
        assertThat(result.getData().getContent().get(0).getName()).isEqualTo("Books");
        verify(categoryRepository).findAll(any(Specification.class), eq(pageable));
    }

    // --------------------------------------------------------------------- get

    @Test
    void shouldThrowNotFoundWhenGetIdMissing() {
        // Arrange (FR-008)
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> categoryController.get(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldGetCategorySuccessfully() {
        // Arrange
        Category category = new Category();
        category.setId(1L);
        category.setName("Category One");

        CategoryDto dto = new CategoryDto();
        dto.setId(1L);
        dto.setName("Category One");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryMapper.fromEntityToCategoryDto(category)).thenReturn(dto);

        // Act
        ApiMessageDto<CategoryDto> result = categoryController.get(1L);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData()).isEqualTo(dto);
    }

    // ------------------------------------------------------------------ delete

    @Test
    void shouldThrowNotFoundWhenDeleteIdMissing() {
        // Arrange (FR-009)
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> categoryController.delete(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowBadRequestWhenDeleteHasChildren() {
        // Arrange (FR-007)
        Category category = new Category();
        category.setId(1L);
        category.setName("Category One");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByParentId(1L)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> categoryController.delete(1L))
                .isInstanceOf(BadRequestException.class);
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void shouldDeleteAvatarFileWhenDeletingCategoryWithNonBlankAvatar() {
        // Arrange (FR-010)
        Category category = new Category();
        category.setId(1L);
        category.setAvatar("/to-delete.png");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByParentId(1L)).thenReturn(false);

        // Act
        ApiMessageDto<Void> result = categoryController.delete(1L);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(fileService).deleteFile("/to-delete.png");
        verify(categoryRepository).deleteById(1L);
    }
}
