package com.ai.api.controller;

import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
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
        if (categoryRepository.existsByNameIgnoreCase(createCategoryForm.getName())) {
            throw new BadRequestException("Category name already exist", ErrorCode.CATEGORY_ERROR_NAME_EXIST);
        }
        Category category = categoryMapper.fromCreateCategoryFormToEntity(createCategoryForm);
        if (createCategoryForm.getParentId() != null) {
            Category parent = categoryRepository.findById(createCategoryForm.getParentId())
                    .orElseThrow(() -> new NotFoundException("Category parent not found", ErrorCode.CATEGORY_ERROR_PARENT_NOT_FOUND));
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
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(updateCategoryForm.getName(), updateCategoryForm.getId())) {
            throw new BadRequestException("Category name already exist", ErrorCode.CATEGORY_ERROR_NAME_EXIST);
        }
        if (updateCategoryForm.getParentId() != null) {
            if (Objects.equals(updateCategoryForm.getParentId(), updateCategoryForm.getId())) {
                throw new BadRequestException("Category parent invalid", ErrorCode.CATEGORY_ERROR_INVALID_PARENT);
            }
            Category parent = categoryRepository.findById(updateCategoryForm.getParentId())
                    .orElseThrow(() -> new NotFoundException("Category parent not found", ErrorCode.CATEGORY_ERROR_PARENT_NOT_FOUND));
            category.setParent(parent);
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
        if (categoryRepository.existsByParentId(id)) {
            throw new BadRequestException("Category has children", ErrorCode.CATEGORY_ERROR_HAS_CHILDREN);
        }
        if (StringUtils.isNoneBlank(category.getAvatar())) {
            fileService.deleteFile(category.getAvatar());
        }
        categoryRepository.deleteById(id);
        return makeSuccessResponse("Delete category success");
    }
}
