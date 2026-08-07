package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.category.CategoryDto;
import com.ai.api.dto.category.CategoryTreeDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.category.CreateCategoryForm;
import com.ai.api.form.category.UpdateCategoryForm;
import com.ai.api.form.category.UpdateCategoryOrderingForm;
import com.ai.api.mapper.CategoryMapper;
import com.ai.api.model.Category;
import com.ai.api.model.criteria.CategoryCriteria;
import com.ai.api.repository.CategoryRepository;
import com.ai.api.repository.NewsRepository;
import com.ai.api.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.validation.BindingResult;

import java.util.Arrays;
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

    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private CategoryController categoryController;

    // ------------------------------------------------------------------ create

    @Test
    void shouldThrowBadRequestWhenCreateRootNameAlreadyExistsAmongRoots() {
        // Arrange (FR-002, scoped: root category name checked among roots only)
        BindingResult bindingResult = mock(BindingResult.class);
        CreateCategoryForm form = new CreateCategoryForm();
        form.setName("Category One");

        when(categoryRepository.existsByNameAndParentIsNull(anyString())).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> categoryController.create(form, bindingResult))
                .isInstanceOf(BadRequestException.class);
        verify(categoryRepository, never()).existsByNameAndParentId(anyString(), any());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenCreateChildNameAlreadyExistsWithinSameParent() {
        // Arrange (FR-002, scoped: child category name checked among siblings under the same parent)
        BindingResult bindingResult = mock(BindingResult.class);
        CreateCategoryForm form = new CreateCategoryForm();
        form.setName("Child One");
        form.setParentId(5L);

        when(categoryRepository.existsByNameAndParentId("Child One", 5L)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> categoryController.create(form, bindingResult))
                .isInstanceOf(BadRequestException.class);
        verify(categoryRepository, never()).existsByNameAndParentIsNull(anyString());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenCreateParentIdUnknown() {
        // Arrange (FR-005)
        BindingResult bindingResult = mock(BindingResult.class);
        CreateCategoryForm form = new CreateCategoryForm();
        form.setName("Category One");
        form.setParentId(99L);

        when(categoryRepository.existsByNameAndParentId(anyString(), eq(99L))).thenReturn(false);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> categoryController.create(form, bindingResult))
                .isInstanceOf(NotFoundException.class);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenCreateParentIsNotRoot() {
        // Arrange (FR-017: max 2-level hierarchy — a child category cannot become a parent)
        BindingResult bindingResult = mock(BindingResult.class);
        CreateCategoryForm form = new CreateCategoryForm();
        form.setName("Grandchild");
        form.setParentId(5L);

        Category root = new Category();
        root.setId(1L);
        root.setName("Root");

        Category childAsParent = new Category();
        childAsParent.setId(5L);
        childAsParent.setName("Child");
        childAsParent.setParent(root);

        when(categoryRepository.existsByNameAndParentId(anyString(), eq(5L))).thenReturn(false);
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(childAsParent));

        // Act + Assert
        assertThatThrownBy(() -> categoryController.create(form, bindingResult))
                .isInstanceOf(BadRequestException.class);
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
    void shouldThrowBadRequestWhenUpdateRootNameExistsAmongRootsExcludingSelf() {
        // Arrange (FR-003, scoped: root category name checked among roots only)
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateCategoryForm form = new UpdateCategoryForm();
        form.setId(1L);
        form.setName("Category Two");

        Category category = new Category();
        category.setId(1L);
        category.setName("Category One");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameAndParentIsNullAndIdNot("Category Two", 1L)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> categoryController.update(form, bindingResult))
                .isInstanceOf(BadRequestException.class);
        verify(categoryRepository, never()).existsByNameAndParentIdAndIdNot(anyString(), any(), any());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenUpdateChildNameExistsWithinSameParentExcludingSelf() {
        // Arrange (FR-003, scoped: child category name checked among siblings under the same parent)
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateCategoryForm form = new UpdateCategoryForm();
        form.setId(2L);
        form.setName("Sibling Two");

        Category parent = new Category();
        parent.setId(1L);
        parent.setName("Parent");

        Category category = new Category();
        category.setId(2L);
        category.setName("Sibling One");
        category.setParent(parent);

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameAndParentIdAndIdNot("Sibling Two", 1L, 2L)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> categoryController.update(form, bindingResult))
                .isInstanceOf(BadRequestException.class);
        verify(categoryRepository, never()).existsByNameAndParentIsNullAndIdNot(anyString(), any());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldNotCheckNameExistsWhenNameUnchangedOnUpdate() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        UpdateCategoryForm form = new UpdateCategoryForm();
        form.setId(1L);
        form.setName("Category One");

        Category category = new Category();
        category.setId(1L);
        category.setName("Category One");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        // Act
        ApiMessageDto<Void> result = categoryController.update(form, bindingResult);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(categoryRepository, never()).existsByNameAndParentIsNullAndIdNot(anyString(), any());
        verify(categoryRepository, never()).existsByNameAndParentIdAndIdNot(anyString(), any(), any());
        verify(categoryRepository).save(category);
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

    @Test
    @SuppressWarnings("unchecked")
    void shouldListCategoriesWithKindAndIsParentFilter() {
        // Arrange (FR-014)
        CategoryCriteria criteria = new CategoryCriteria();
        criteria.setKind(1);
        criteria.setIsParent(true);
        Pageable pageable = PageRequest.of(0, 10);

        Category category = new Category();
        category.setId(1L);
        category.setName("Books");
        category.setKind(1);
        Page<Category> page = new PageImpl<>(Collections.singletonList(category), pageable, 1);

        CategoryDto dto = new CategoryDto();
        dto.setId(1L);
        dto.setName("Books");
        dto.setKind(1);

        when(categoryRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(categoryMapper.fromEntityToCategoryDtoList(anyList())).thenReturn(Collections.singletonList(dto));

        // Act
        ApiMessageDto<ResponseListDto<List<CategoryDto>>> result = categoryController.list(criteria, pageable);

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).hasSize(1);
        assertThat(result.getData().getContent().get(0).getKind()).isEqualTo(1);
        verify(categoryRepository).findAll(any(Specification.class), eq(pageable));
    }

    // ------------------------------------------------------------ auto-complete

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnTrimmedIdAndNameOnlyForAutoComplete() {
        // Arrange (FR-019: auto-complete trims CategoryDto down to id + name only, page fixed to (0, 10))
        CategoryCriteria criteria = new CategoryCriteria();
        criteria.setName("Book");
        Pageable pageable = PageRequest.of(0, 10);

        Category category = new Category();
        category.setId(1L);
        category.setName("Books");
        category.setDescription("Book category");
        category.setAvatar("/books.png");
        category.setKind(1);
        category.setOrdering(2);
        Page<Category> page = new PageImpl<>(Collections.singletonList(category), pageable, 1);

        CategoryDto dto = new CategoryDto();
        dto.setId(1L);
        dto.setName("Books");

        when(categoryRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(categoryMapper.fromEntityToCategoryAutoCompleteDtoList(anyList())).thenReturn(Collections.singletonList(dto));

        // Act
        ApiMessageDto<ResponseListDto<List<CategoryDto>>> result = categoryController.autoComplete(criteria);

        // Assert
        assertThat(criteria.getStatus()).isEqualTo(AIConstant.STATUS_ACTIVE);
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).hasSize(1);
        CategoryDto returned = result.getData().getContent().get(0);
        assertThat(returned.getId()).isEqualTo(1L);
        assertThat(returned.getName()).isEqualTo("Books");
        assertThat(returned.getDescription()).isNull();
        assertThat(returned.getAvatar()).isNull();
        assertThat(returned.getParentId()).isNull();
        assertThat(returned.getKind()).isNull();
        assertThat(returned.getOrdering()).isNull();
        verify(categoryRepository).findAll(any(Specification.class), eq(pageable));
        verify(categoryMapper, never()).fromEntityToCategoryDtoList(anyList());
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
    void shouldCascadeDeleteChildrenWhenDeletingParentCategory() {
        // Arrange (FR-007, amended: cascade one level instead of rejecting)
        Category category = new Category();
        category.setId(1L);
        category.setName("Category One");

        Category child = new Category();
        child.setId(2L);
        child.setParent(category);
        child.setAvatar("/child-avatar.png");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByParentIdIn(Collections.singletonList(1L)))
                .thenReturn(Collections.singletonList(child));

        // Act
        ApiMessageDto<Void> result = categoryController.delete(1L);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(fileService).deleteFile("/child-avatar.png");
        verify(categoryRepository).deleteAll(Collections.singletonList(child));
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void shouldDeleteAvatarFileWhenDeletingCategoryWithNonBlankAvatar() {
        // Arrange (FR-010)
        Category category = new Category();
        category.setId(1L);
        category.setAvatar("/to-delete.png");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByParentIdIn(Collections.singletonList(1L)))
                .thenReturn(Collections.emptyList());

        // Act
        ApiMessageDto<Void> result = categoryController.delete(1L);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(fileService).deleteFile("/to-delete.png");
        verify(categoryRepository).deleteById(1L);
    }

    // ------------------------------------------------------------- public list

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnParentCategoriesWithChildrenInPublicList() {
        // Arrange (FR-015)
        CategoryCriteria criteria = new CategoryCriteria();
        Pageable pageable = PageRequest.of(0, 10);

        Category parentA = new Category();
        parentA.setId(1L);
        parentA.setName("Parent A");
        parentA.setOrdering(0);

        Category parentD = new Category();
        parentD.setId(4L);
        parentD.setName("Parent D");
        parentD.setOrdering(1);

        Page<Category> parentsPage = new PageImpl<>(Arrays.asList(parentA, parentD), pageable, 2);

        Category childB = new Category();
        childB.setId(2L);
        childB.setName("Child B");
        childB.setParent(parentA);
        childB.setOrdering(0);

        Category childC = new Category();
        childC.setId(3L);
        childC.setName("Child C");
        childC.setParent(parentA);
        childC.setOrdering(1);

        CategoryTreeDto dtoA = new CategoryTreeDto();
        dtoA.setId(1L);
        dtoA.setName("Parent A");

        CategoryTreeDto dtoD = new CategoryTreeDto();
        dtoD.setId(4L);
        dtoD.setName("Parent D");

        CategoryDto dtoB = new CategoryDto();
        dtoB.setId(2L);
        dtoB.setName("Child B");

        CategoryDto dtoC = new CategoryDto();
        dtoC.setId(3L);
        dtoC.setName("Child C");

        when(categoryRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(parentsPage);
        when(categoryRepository.findByParentIdIn(Arrays.asList(1L, 4L)))
                .thenReturn(Arrays.asList(childB, childC));
        when(categoryMapper.fromEntityToCategoryTreeDtoList(Arrays.asList(parentA, parentD)))
                .thenReturn(Arrays.asList(dtoA, dtoD));
        when(categoryMapper.fromEntityToCategoryDtoList(eq(Arrays.asList(childB, childC))))
                .thenReturn(Arrays.asList(dtoB, dtoC));
        when(categoryMapper.fromEntityToCategoryDtoList(eq(Collections.emptyList())))
                .thenReturn(Collections.emptyList());

        // Act
        ApiMessageDto<ResponseListDto<List<CategoryTreeDto>>> result = categoryController.publicList(criteria, pageable);

        // Assert
        assertThat(criteria.getIsParent()).isTrue();
        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).hasSize(2);
        assertThat(result.getData().getContent().get(0).getChildren()).hasSize(2);
        assertThat(result.getData().getContent().get(0).getChildren()).extracting(CategoryDto::getName)
                .containsExactly("Child B", "Child C");
        assertThat(result.getData().getContent().get(1).getChildren()).isEmpty();
    }

    // --------------------------------------------------------- update-ordering

    @Test
    void shouldUpdateOrderingForMultipleCategories() {
        // Arrange (FR-016)
        UpdateCategoryOrderingForm form1 = new UpdateCategoryOrderingForm();
        form1.setId(1L);
        form1.setOrdering(2);

        UpdateCategoryOrderingForm form2 = new UpdateCategoryOrderingForm();
        form2.setId(2L);
        form2.setOrdering(1);

        Category category1 = new Category();
        category1.setId(1L);
        category1.setOrdering(0);

        Category category2 = new Category();
        category2.setId(2L);
        category2.setOrdering(1);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(category2));

        // Act
        ApiMessageDto<Void> result = categoryController.updateOrdering(Arrays.asList(form1, form2));

        // Assert
        assertThat(result.getResult()).isTrue();
        assertThat(category1.getOrdering()).isEqualTo(2);
        assertThat(category2.getOrdering()).isEqualTo(1);
        verify(categoryRepository, times(1)).saveAll(eq(Arrays.asList(category1, category2)));
    }

    @Test
    void shouldThrowNotFoundWhenUpdateOrderingIdUnknown() {
        // Arrange (FR-016)
        UpdateCategoryOrderingForm form1 = new UpdateCategoryOrderingForm();
        form1.setId(1L);
        form1.setOrdering(2);

        UpdateCategoryOrderingForm form2 = new UpdateCategoryOrderingForm();
        form2.setId(99L);
        form2.setOrdering(1);

        Category category1 = new Category();
        category1.setId(1L);
        category1.setOrdering(0);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> categoryController.updateOrdering(Arrays.asList(form1, form2)))
                .isInstanceOf(NotFoundException.class);
        verify(categoryRepository, never()).saveAll(anyList());
    }

    // -------------------------------------------------------- cascade delete news

    @Test
    void shouldCascadeDeleteNewsWhenDeletingRootAndChildCategories() {
        // Arrange (FR-009 category / FR-018 news: cascade delete news for root + cascaded child)
        Category root = new Category();
        root.setId(1L);
        root.setName("Root");

        Category child = new Category();
        child.setId(2L);
        child.setName("Child");
        child.setParent(root);

        List<Long> categoryIds = Arrays.asList(1L, 2L);
        List<String> newsAvatars = Arrays.asList("/root-news-avatar.png", "/child-news-avatar.png");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));
        when(categoryRepository.findByParentIdIn(Collections.singletonList(1L)))
                .thenReturn(Collections.singletonList(child));
        when(newsRepository.findAvatarsByCategoryIdIn(categoryIds)).thenReturn(newsAvatars);

        // Act
        ApiMessageDto<Void> result = categoryController.delete(1L);

        // Assert
        assertThat(result.getResult()).isTrue();
        verify(fileService, times(1)).deleteFiles(newsAvatars);
        verify(newsRepository, times(1)).deleteAllByCategoryIdIn(categoryIds);

        InOrder inOrder = inOrder(fileService, newsRepository, categoryRepository);
        inOrder.verify(fileService).deleteFiles(newsAvatars);
        inOrder.verify(newsRepository).deleteAllByCategoryIdIn(categoryIds);
        inOrder.verify(categoryRepository).deleteById(1L);
    }
}
