package com.ai.api.controller;

import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.syllabusmaterial.SyllabusMaterialDto;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.syllabusmaterial.CreateSyllabusMaterialForm;
import com.ai.api.form.syllabusmaterial.UpdateSyllabusMaterialForm;
import com.ai.api.mapper.SyllabusMaterialMapper;
import com.ai.api.model.Syllabus;
import com.ai.api.model.SyllabusMaterial;
import com.ai.api.model.criteria.SyllabusMaterialCriteria;
import com.ai.api.repository.SyllabusMaterialRepository;
import com.ai.api.repository.SyllabusRepository;
import com.ai.api.service.FileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyllabusMaterialControllerTest {

    @Mock
    private SyllabusMaterialRepository syllabusMaterialRepository;

    @Mock
    private SyllabusRepository syllabusRepository;

    @Mock
    private SyllabusMaterialMapper syllabusMaterialMapper;

    @Mock
    private FileService fileService;

    @InjectMocks
    private SyllabusMaterialController controller;

    @Test
    void shouldCreateSyllabusMaterialWhenSyllabusExists() {
        CreateSyllabusMaterialForm form = new CreateSyllabusMaterialForm();
        form.setSyllabusId(1L);
        form.setTitle("title");
        form.setFileUrl("file.pdf");

        Syllabus syllabus = new Syllabus();
        syllabus.setId(1L);
        SyllabusMaterial entity = new SyllabusMaterial();

        when(syllabusRepository.findById(1L)).thenReturn(Optional.of(syllabus));
        when(syllabusMaterialMapper.fromCreateSyllabusMaterialFormToEntity(form)).thenReturn(entity);

        ApiMessageDto<SyllabusMaterialDto> result = controller.create(form, null);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Create syllabus material success");
        assertThat(entity.getSyllabus()).isSameAs(syllabus);
        verify(syllabusMaterialRepository).save(entity);
    }

    @Test
    void shouldThrowSyllabusNotFoundWhenCreateWithUnknownSyllabusId() {
        CreateSyllabusMaterialForm form = new CreateSyllabusMaterialForm();
        form.setSyllabusId(99L);
        form.setTitle("title");
        form.setFileUrl("file.pdf");

        when(syllabusRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.create(form, null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.SYLLABUS_ERROR_NOT_FOUND);
    }

    @Test
    void shouldReturnSyllabusMaterialDtoWhenIdExists() {
        SyllabusMaterial entity = new SyllabusMaterial();
        SyllabusMaterialDto dto = new SyllabusMaterialDto();

        when(syllabusMaterialRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(syllabusMaterialMapper.fromEntityToSyllabusMaterialDto(entity)).thenReturn(dto);

        ApiMessageDto<SyllabusMaterialDto> result = controller.get(1L);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getData()).isSameAs(dto);
        assertThat(result.getMessage()).isEqualTo("Get syllabus material success");
    }

    @Test
    void shouldThrowNotFoundWhenGetIdDoesNotExist() {
        when(syllabusMaterialRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.get(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.SYLLABUS_MATERIAL_ERROR_NOT_FOUND);
    }

    @Test
    void shouldListSyllabusMaterialsFilteredBySyllabusIdAndTitle() {
        SyllabusMaterialCriteria criteria = new SyllabusMaterialCriteria();
        criteria.setSyllabusId(1L);
        criteria.setTitle("keyword");
        Pageable pageable = mock(Pageable.class);

        SyllabusMaterial entity = new SyllabusMaterial();
        SyllabusMaterialDto dto = new SyllabusMaterialDto();
        Page<SyllabusMaterial> page = new PageImpl<>(Collections.singletonList(entity));

        when(syllabusMaterialRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(syllabusMaterialMapper.fromEntityToSyllabusMaterialDtoList(page.getContent()))
                .thenReturn(Collections.singletonList(dto));

        ApiMessageDto<ResponseListDto<List<SyllabusMaterialDto>>> result = controller.list(criteria, pageable);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).containsExactly(dto);
        assertThat(result.getData().getTotalElements()).isEqualTo(1);
        assertThat(result.getData().getTotalPages()).isEqualTo(1);
    }

    @Test
    void shouldDeleteOldFileWhenUpdateChangesFileUrl() {
        SyllabusMaterial entity = new SyllabusMaterial();
        entity.setId(1L);
        entity.setFileUrl("old.pdf");

        UpdateSyllabusMaterialForm form = new UpdateSyllabusMaterialForm();
        form.setId(1L);
        form.setTitle("new title");
        form.setFileUrl("new.pdf");

        when(syllabusMaterialRepository.findById(1L)).thenReturn(Optional.of(entity));

        ApiMessageDto<Void> result = controller.update(form, null);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Update syllabus material success");
        verify(fileService).deleteFile("old.pdf");
        verify(syllabusMaterialMapper).updateEntityFromForm(form, entity);
        verify(syllabusMaterialRepository).save(entity);
    }

    @Test
    void shouldNotDeleteFileWhenUpdateKeepsSameFileUrl() {
        SyllabusMaterial entity = new SyllabusMaterial();
        entity.setId(1L);
        entity.setFileUrl("same.pdf");

        UpdateSyllabusMaterialForm form = new UpdateSyllabusMaterialForm();
        form.setId(1L);
        form.setTitle("new title");
        form.setFileUrl("same.pdf");

        when(syllabusMaterialRepository.findById(1L)).thenReturn(Optional.of(entity));

        controller.update(form, null);

        verify(fileService, never()).deleteFile(any());
    }

    @Test
    void shouldThrowNotFoundWhenUpdateIdDoesNotExist() {
        UpdateSyllabusMaterialForm form = new UpdateSyllabusMaterialForm();
        form.setId(1L);
        form.setTitle("title");
        form.setFileUrl("file.pdf");

        when(syllabusMaterialRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.update(form, null))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.SYLLABUS_MATERIAL_ERROR_NOT_FOUND);
    }

    @Test
    void shouldDeleteFileWhenDeleteWithNonBlankFileUrl() {
        SyllabusMaterial entity = new SyllabusMaterial();
        entity.setId(1L);
        entity.setFileUrl("file.pdf");

        when(syllabusMaterialRepository.findById(1L)).thenReturn(Optional.of(entity));

        ApiMessageDto<Void> result = controller.delete(1L);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Delete syllabus material success");
        verify(fileService).deleteFile("file.pdf");
        verify(syllabusMaterialRepository).deleteById(1L);
    }

    @Test
    void shouldNotDeleteFileWhenDeleteWithBlankFileUrl() {
        SyllabusMaterial entity = new SyllabusMaterial();
        entity.setId(1L);
        entity.setFileUrl("");

        when(syllabusMaterialRepository.findById(1L)).thenReturn(Optional.of(entity));

        controller.delete(1L);

        verify(fileService, never()).deleteFile(any());
        verify(syllabusMaterialRepository).deleteById(1L);
    }

    @Test
    void shouldThrowNotFoundWhenDeleteIdDoesNotExist() {
        when(syllabusMaterialRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.SYLLABUS_MATERIAL_ERROR_NOT_FOUND);
    }
}
