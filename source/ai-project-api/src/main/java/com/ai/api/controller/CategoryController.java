package com.ai.api.controller;

import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/category")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class CategoryController extends ABasicController {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private FileService fileService;

    @Transactional
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CAT_C')")
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateCategoryForm createCategoryForm, BindingResult bindingResult) {
        Long parentId = createCategoryForm.getParentId();
        boolean nameExists = parentId == null
                ? categoryRepository.existsByNameAndParentIsNull(createCategoryForm.getName())
                : categoryRepository.existsByNameAndParentId(createCategoryForm.getName(), parentId);
        if (nameExists) {
            throw new BadRequestException("Category name already exist", ErrorCode.CATEGORY_ERROR_NAME_EXIST);
        }
        Category category = categoryMapper.fromCreateCategoryFormToEntity(createCategoryForm);
        if (parentId != null) {
            Category parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new NotFoundException("Category parent not found", ErrorCode.CATEGORY_ERROR_PARENT_NOT_FOUND));
            if (parent.getParent() != null) {
                throw new BadRequestException("Category parent must be a root category", ErrorCode.CATEGORY_ERROR_PARENT_NOT_ROOT);
            }
            category.setParent(parent);
        }
        categoryRepository.save(category);
        return makeSuccessResponse("Create category success");
    }

    @Transactional
    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CAT_U')")
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateCategoryForm updateCategoryForm, BindingResult bindingResult) {
        Category category = categoryRepository.findById(updateCategoryForm.getId())
                .orElseThrow(() -> new NotFoundException("Category not found", ErrorCode.CATEGORY_ERROR_NOT_FOUND));
        if (!Objects.equals(updateCategoryForm.getName(), category.getName())) {
            Long parentId = category.getParent() != null ? category.getParent().getId() : null;
            boolean nameExists = parentId == null
                    ? categoryRepository.existsByNameAndParentIsNullAndIdNot(updateCategoryForm.getName(), category.getId())
                    : categoryRepository.existsByNameAndParentIdAndIdNot(updateCategoryForm.getName(), parentId, category.getId());
            if (nameExists) {
                throw new BadRequestException("Category name already exist", ErrorCode.CATEGORY_ERROR_NAME_EXIST);
            }
        }
        String oldAvatar = category.getAvatar();
        if (StringUtils.isNoneBlank(oldAvatar) && !Objects.equals(updateCategoryForm.getAvatar(), oldAvatar)) {
            fileService.deleteFile(oldAvatar);
        }
        categoryMapper.updateEntityFromForm(updateCategoryForm, category);
        categoryRepository.save(category);
        return makeSuccessResponse("Update category success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CAT_V')")
    public ApiMessageDto<CategoryDto> get(@PathVariable("id") Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found", ErrorCode.CATEGORY_ERROR_NOT_FOUND));
        return makeSuccessResponse(categoryMapper.fromEntityToCategoryDto(category), "Get category success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CAT_L')")
    public ApiMessageDto<ResponseListDto<List<CategoryDto>>> list(CategoryCriteria criteria, Pageable pageable) {
        Page<Category> categories = categoryRepository.findAll(criteria.getCriteria(), pageable);
        return makeSuccessResponse(makeResponseListDto(categories, categoryMapper::fromEntityToCategoryDtoList), "List category success");
    }

    @Transactional
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CAT_D')")
    public ApiMessageDto<Void> delete(@PathVariable("id") Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found", ErrorCode.CATEGORY_ERROR_NOT_FOUND));
        List<Category> children = categoryRepository.findByParentIdIn(Collections.singletonList(id));
        for (Category child : children) {
            if (StringUtils.isNoneBlank(child.getAvatar())) {
                fileService.deleteFile(child.getAvatar());
            }
        }
        categoryRepository.deleteAll(children);
        if (StringUtils.isNoneBlank(category.getAvatar())) {
            fileService.deleteFile(category.getAvatar());
        }
        categoryRepository.deleteById(id);
        return makeSuccessResponse("Delete category success");
    }

    @GetMapping(value = "/public/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<CategoryTreeDto>>> publicList(CategoryCriteria criteria, Pageable pageable) {
        criteria.setIsParent(true);
        Page<Category> parents = categoryRepository.findAll(criteria.getCriteria(), pageable);
        return makeSuccessResponse(makeResponseListDto(parents, this::toCategoryTreeDtoList), "List category tree success");
    }

    private List<CategoryTreeDto> toCategoryTreeDtoList(List<Category> parents) {
        List<Long> parentIds = parents.stream().map(Category::getId).collect(Collectors.toList());
        Map<Long, List<Category>> childrenByParentId = categoryRepository.findByParentIdIn(parentIds).stream()
                .collect(Collectors.groupingBy(category -> category.getParent().getId(), LinkedHashMap::new, Collectors.toList()));

        List<CategoryTreeDto> trees = categoryMapper.fromEntityToCategoryTreeDtoList(parents);
        for (CategoryTreeDto tree : trees) {
            List<Category> children = childrenByParentId.getOrDefault(tree.getId(), Collections.emptyList());
            tree.setChildren(categoryMapper.fromEntityToCategoryDtoList(children));
        }
        return trees;
    }

    @Transactional
    @PutMapping(value = "/update-ordering", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('CAT_U')")
    public ApiMessageDto<Void> updateOrdering(@Valid @RequestBody List<UpdateCategoryOrderingForm> forms) {
        List<Category> categories = new ArrayList<>();
        for (UpdateCategoryOrderingForm form : forms) {
            Category category = categoryRepository.findById(form.getId())
                    .orElseThrow(() -> new NotFoundException("Category not found", ErrorCode.CATEGORY_ERROR_NOT_FOUND));
            category.setOrdering(form.getOrdering());
            categories.add(category);
        }
        categoryRepository.saveAll(categories);
        return makeSuccessResponse("Update category ordering success");
    }
}
