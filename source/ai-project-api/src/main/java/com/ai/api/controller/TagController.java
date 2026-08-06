package com.ai.api.controller;

import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.tag.TagDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.tag.CreateTagForm;
import com.ai.api.form.tag.UpdateTagForm;
import com.ai.api.mapper.TagMapper;
import com.ai.api.model.Tag;
import com.ai.api.model.criteria.TagCriteria;
import com.ai.api.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;
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

@RestController
@RequestMapping("/v1/tags")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class TagController extends ABasicController {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TagMapper tagMapper;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('TAG_C')")
    @Transactional
    public ApiMessageDto<Void> create(@Valid @RequestBody CreateTagForm createTagForm, BindingResult bindingResult) {
        if (tagRepository.existsByNameIgnoreCase(createTagForm.getName())) {
            throw new BadRequestException("Tag already exist", ErrorCode.TAG_ERROR_IS_EXISTED);
        }
        Tag tag = tagMapper.fromCreateFormToEntity(createTagForm);
        tagRepository.save(tag);
        return makeSuccessResponse("Create tag success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('TAG_U')")
    @Transactional
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateTagForm updateTagForm, BindingResult bindingResult) {
        Tag tag = tagRepository.findById(updateTagForm.getId())
                .orElseThrow(() -> new NotFoundException("Not found tag!", ErrorCode.TAG_ERROR_NOT_FOUND));

        if (!tag.getName().equalsIgnoreCase(updateTagForm.getName())
                && tagRepository.existsByNameIgnoreCase(updateTagForm.getName())) {
            throw new BadRequestException("Tag already exist", ErrorCode.TAG_ERROR_IS_EXISTED);
        }

        tagMapper.fromUpdateFormToEntity(updateTagForm, tag);
        tagRepository.save(tag);
        return makeSuccessResponse("Update tag success");
    }

    @DeleteMapping(value = "/delete/{id}")
    @PreAuthorize("hasRole('TAG_D')")
    @Transactional
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found tag!", ErrorCode.TAG_ERROR_NOT_FOUND));
        tagRepository.delete(tag);
        return makeSuccessResponse("Delete tag success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('TAG_V')")
    public ApiMessageDto<TagDto> get(@PathVariable Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found tag!", ErrorCode.TAG_ERROR_NOT_FOUND));
        return makeSuccessResponse(tagMapper.fromEntityToTagDto(tag), "Get tag success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('TAG_L')")
    public ApiMessageDto<ResponseListDto<List<TagDto>>> list(TagCriteria tagCriteria, Pageable pageable) {
        Page<Tag> page = tagRepository.findAll(tagCriteria.getCriteria(), pageable);
        ResponseListDto<List<TagDto>> responseListDto =
                makeResponseListDto(page, tagMapper::fromEntityListToTagDtoList);
        return makeSuccessResponse(responseListDto, "Get list tag success");
    }
}
