package com.ai.api.controller;

import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.tag.TagDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.tag.CreateTagForm;
import com.ai.api.form.tag.UpdateTagForm;
import com.ai.api.mapper.TagMapper;
import com.ai.api.model.Tag;
import com.ai.api.repository.TagRepository;
import com.ai.api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagControllerTest {

    @Mock private TagRepository tagRepository;
    @Mock private TagMapper tagMapper;
    @Mock private UserServiceImpl userService;
    @Mock private BindingResult bindingResult;
    @InjectMocks private TagController tagController;

    @Test
    void create_whenNameNotExisted_returnsSuccessResponse() {
        CreateTagForm form = new CreateTagForm();
        form.setName("Java");
        Tag tag = new Tag();
        when(tagRepository.existsByNameIgnoreCase("Java")).thenReturn(false);
        when(tagMapper.fromCreateFormToEntity(form)).thenReturn(tag);

        ApiMessageDto<TagDto> result = tagController.create(form, bindingResult);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Create tag success");
        verify(tagRepository).save(tag);
    }

    @Test
    void create_whenNameExisted_throwsBadRequestException() {
        CreateTagForm form = new CreateTagForm();
        form.setName("Java");
        when(tagRepository.existsByNameIgnoreCase("Java")).thenReturn(true);

        assertThatThrownBy(() -> tagController.create(form, bindingResult))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.TAG_ERROR_IS_EXISTED);
    }

    @Test
    void update_whenNameNotChangedAndFound_returnsSuccessResponse() {
        UpdateTagForm form = new UpdateTagForm();
        form.setId(1L);
        form.setName("Java");
        Tag tag = new Tag();
        tag.setName("Java");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        ApiMessageDto<Void> result = tagController.update(form, bindingResult);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Update tag success");
        verify(tagRepository).save(tag);
    }

    @Test
    void update_whenNameChangedToExistingName_throwsBadRequestException() {
        UpdateTagForm form = new UpdateTagForm();
        form.setId(1L);
        form.setName("Python");
        Tag tag = new Tag();
        tag.setName("Java");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.existsByNameIgnoreCase("Python")).thenReturn(true);

        assertThatThrownBy(() -> tagController.update(form, bindingResult))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.TAG_ERROR_IS_EXISTED);
    }

    @Test
    void update_whenNotFound_throwsNotFoundException() {
        UpdateTagForm form = new UpdateTagForm();
        form.setId(1L);
        form.setName("Java");
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagController.update(form, bindingResult))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.TAG_ERROR_NOT_FOUND);
    }

    @Test
    void delete_whenNotFound_throwsNotFoundException() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagController.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.TAG_ERROR_NOT_FOUND);
    }

    @Test
    void delete_whenFound_returnsSuccessResponse() {
        Tag tag = new Tag();
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        ApiMessageDto<Void> result = tagController.delete(1L);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Delete tag success");
        verify(tagRepository).delete(tag);
    }

    @Test
    void get_whenNotFound_throwsNotFoundException() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagController.get(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.TAG_ERROR_NOT_FOUND);
    }

    @Test
    void get_whenFound_returnsSuccessDto() {
        Tag tag = new Tag();
        TagDto dto = new TagDto();
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagMapper.fromEntityToTagDto(tag)).thenReturn(dto);

        ApiMessageDto<TagDto> result = tagController.get(1L);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getData()).isSameAs(dto);
        assertThat(result.getMessage()).isEqualTo("Get tag success");
    }
}
