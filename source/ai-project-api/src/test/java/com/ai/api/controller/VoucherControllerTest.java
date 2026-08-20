package com.ai.api.controller;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ApiMessageDto;
import com.ai.api.dto.ErrorCode;
import com.ai.api.dto.ResponseListDto;
import com.ai.api.dto.voucher.VoucherDto;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.form.voucher.CreateVoucherForm;
import com.ai.api.form.voucher.UpdateVoucherForm;
import com.ai.api.mapper.VoucherMapper;
import com.ai.api.model.Voucher;
import com.ai.api.model.criteria.VoucherCriteria;
import com.ai.api.repository.VoucherRepository;
import com.ai.api.service.impl.UserServiceImpl;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherControllerTest {

    @Mock private VoucherRepository voucherRepository;
    @Mock private VoucherMapper voucherMapper;
    @Mock private UserServiceImpl userService;
    @Mock private BindingResult bindingResult;
    @InjectMocks private VoucherController voucherController;

    @Test
    void create_whenCodeNotExisted_returnsSuccessResponse() {
        CreateVoucherForm form = new CreateVoucherForm();
        form.setCode("SALE10");
        Voucher voucher = new Voucher();
        when(voucherRepository.existsByCodeIgnoreCase("SALE10")).thenReturn(false);
        when(voucherMapper.fromCreateFormToEntity(form)).thenReturn(voucher);

        ApiMessageDto<VoucherDto> result = voucherController.create(form, bindingResult);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Create voucher success");
        verify(voucherRepository).save(voucher);
    }

    @Test
    void create_whenCodeExisted_throwsBadRequestException() {
        CreateVoucherForm form = new CreateVoucherForm();
        form.setCode("SALE10");
        when(voucherRepository.existsByCodeIgnoreCase("SALE10")).thenReturn(true);

        assertThatThrownBy(() -> voucherController.create(form, bindingResult))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.VOUCHER_ERROR_CODE_EXISTED);
    }

    @Test
    void update_whenCodeNotChangedAndFound_returnsSuccessResponse() {
        UpdateVoucherForm form = new UpdateVoucherForm();
        form.setId(1L);
        form.setCode("SALE10");
        Voucher voucher = new Voucher();
        voucher.setCode("SALE10");
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(voucher));

        ApiMessageDto<Void> result = voucherController.update(form, bindingResult);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Update voucher success");
        verify(voucherRepository).save(voucher);
    }

    @Test
    void update_whenCodeChangedToExistingOtherCode_throwsBadRequestException() {
        UpdateVoucherForm form = new UpdateVoucherForm();
        form.setId(1L);
        form.setCode("SALE20");
        Voucher voucher = new Voucher();
        voucher.setCode("SALE10");
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(voucher));
        when(voucherRepository.existsByCodeIgnoreCase("SALE20")).thenReturn(true);

        assertThatThrownBy(() -> voucherController.update(form, bindingResult))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.VOUCHER_ERROR_CODE_EXISTED);
    }

    @Test
    void update_whenNotFound_throwsNotFoundException() {
        UpdateVoucherForm form = new UpdateVoucherForm();
        form.setId(1L);
        form.setCode("SALE10");
        when(voucherRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> voucherController.update(form, bindingResult))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.VOUCHER_ERROR_NOT_FOUND);
    }

    @Test
    void delete_whenFound_returnsSuccessResponse() {
        Voucher voucher = new Voucher();
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(voucher));

        ApiMessageDto<Void> result = voucherController.delete(1L);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getMessage()).isEqualTo("Delete voucher success");
        verify(voucherRepository).delete(voucher);
    }

    @Test
    void get_whenNotFound_throwsNotFoundException() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> voucherController.get(1L))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.VOUCHER_ERROR_NOT_FOUND);
    }

    @Test
    void get_whenFound_returnsSuccessDto() {
        Voucher voucher = new Voucher();
        VoucherDto dto = new VoucherDto();
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(voucher));
        when(voucherMapper.fromEntityToVoucherDto(voucher)).thenReturn(dto);

        ApiMessageDto<VoucherDto> result = voucherController.get(1L);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getData()).isSameAs(dto);
        assertThat(result.getMessage()).isEqualTo("Get voucher success");
    }

    @Test
    void list_delegatesToRepositoryFindAllWithCriteriaAndPageable() {
        VoucherCriteria criteria = new VoucherCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Voucher voucher = new Voucher();
        VoucherDto dto = new VoucherDto();
        Page<Voucher> page = new PageImpl<>(Collections.singletonList(voucher));
        when(voucherRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(voucherMapper.fromEntityListToVoucherDtoList(page.getContent()))
                .thenReturn(Collections.singletonList(dto));

        ApiMessageDto<ResponseListDto<List<VoucherDto>>> result = voucherController.list(criteria, pageable);

        assertThat(result.getResult()).isTrue();
        assertThat(result.getData().getContent()).containsExactly(dto);
        verify(voucherRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getByCode_whenFound_returnsSuccessDto() {
        Voucher voucher = new Voucher();
        VoucherDto dto = new VoucherDto();
        when(voucherRepository.findByCodeIgnoreCase("SALE10")).thenReturn(Optional.of(voucher));
        when(voucherMapper.fromEntityToVoucherDto(voucher)).thenReturn(dto);

        ApiMessageDto<VoucherDto> result = voucherController.getByCode("SALE10");

        assertThat(result.getResult()).isTrue();
        assertThat(result.getData()).isSameAs(dto);
    }

    @Test
    void getByCode_whenNotFound_throwsNotFoundException() {
        when(voucherRepository.findByCodeIgnoreCase("SALE10")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> voucherController.getByCode("SALE10"))
                .isInstanceOf(NotFoundException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.VOUCHER_ERROR_NOT_FOUND);
    }

    @Test
    void done_whenStatePending_deletesVoucherAndDoesNotSave() {
        Voucher voucher = new Voucher();
        voucher.setState(AIConstant.VOUCHER_STATE_PENDING);
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(voucher));

        ApiMessageDto<Void> result = voucherController.done(1L);

        assertThat(result.getResult()).isTrue();
        verify(voucherRepository).delete(voucher);
        verify(voucherRepository, never()).save(any(Voucher.class));
    }

    @Test
    void done_whenStateActive_savesVoucherWithStateDone() {
        Voucher voucher = new Voucher();
        voucher.setState(AIConstant.VOUCHER_STATE_ACTIVE);
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(voucher));

        ApiMessageDto<Void> result = voucherController.done(1L);

        assertThat(result.getResult()).isTrue();
        assertThat(voucher.getState()).isEqualTo(AIConstant.VOUCHER_STATE_DONE);
        verify(voucherRepository).save(voucher);
        verify(voucherRepository, never()).delete(any(Voucher.class));
    }

    @Test
    void done_whenStateDone_throwsBadRequestExceptionAndDoesNotMutate() {
        Voucher voucher = new Voucher();
        voucher.setState(AIConstant.VOUCHER_STATE_DONE);
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(voucher));

        assertThatThrownBy(() -> voucherController.done(1L))
                .isInstanceOf(BadRequestException.class)
                .hasFieldOrPropertyWithValue("code", ErrorCode.VOUCHER_ERROR_ALREADY_DONE);

        verify(voucherRepository, never()).save(any(Voucher.class));
        verify(voucherRepository, never()).delete(any(Voucher.class));
    }
}
