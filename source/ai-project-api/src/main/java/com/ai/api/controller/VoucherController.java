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
@RequestMapping("/v1/vouchers")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class VoucherController extends ABasicController {

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private VoucherMapper voucherMapper;

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('VOU_C')")
    @Transactional
    public ApiMessageDto<VoucherDto> create(@Valid @RequestBody CreateVoucherForm createVoucherForm, BindingResult bindingResult) {
        if (voucherRepository.existsByCodeIgnoreCase(createVoucherForm.getCode())) {
            throw new BadRequestException("Voucher code already exist", ErrorCode.VOUCHER_ERROR_CODE_EXISTED);
        }
        Voucher voucher = voucherMapper.fromCreateFormToEntity(createVoucherForm);
        voucherRepository.save(voucher);
        return makeSuccessResponse(voucherMapper.fromEntityToVoucherIdDto(voucher), "Create voucher success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('VOU_U')")
    @Transactional
    public ApiMessageDto<Void> update(@Valid @RequestBody UpdateVoucherForm updateVoucherForm, BindingResult bindingResult) {
        Voucher voucher = voucherRepository.findById(updateVoucherForm.getId())
                .orElseThrow(() -> new NotFoundException("Not found voucher!", ErrorCode.VOUCHER_ERROR_NOT_FOUND));

        if (!voucher.getCode().equalsIgnoreCase(updateVoucherForm.getCode())
                && voucherRepository.existsByCodeIgnoreCase(updateVoucherForm.getCode())) {
            throw new BadRequestException("Voucher code already exist", ErrorCode.VOUCHER_ERROR_CODE_EXISTED);
        }

        voucherMapper.updateEntityFromForm(updateVoucherForm, voucher);
        voucherRepository.save(voucher);
        return makeSuccessResponse("Update voucher success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('VOU_D')")
    @Transactional
    public ApiMessageDto<Void> delete(@PathVariable Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found voucher!", ErrorCode.VOUCHER_ERROR_NOT_FOUND));
        voucherRepository.delete(voucher);
        return makeSuccessResponse("Delete voucher success");
    }

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('VOU_V')")
    public ApiMessageDto<VoucherDto> get(@PathVariable Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found voucher!", ErrorCode.VOUCHER_ERROR_NOT_FOUND));
        return makeSuccessResponse(voucherMapper.fromEntityToVoucherDto(voucher), "Get voucher success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('VOU_L')")
    public ApiMessageDto<ResponseListDto<List<VoucherDto>>> list(VoucherCriteria voucherCriteria, Pageable pageable) {
        Page<Voucher> page = voucherRepository.findAll(voucherCriteria.getCriteria(), pageable);
        ResponseListDto<List<VoucherDto>> responseListDto =
                makeResponseListDto(page, voucherMapper::fromEntityListToVoucherDtoList);
        return makeSuccessResponse(responseListDto, "Get list voucher success");
    }

    @GetMapping(value = "/get-by-code/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<VoucherDto> getByCode(@PathVariable String code) {
        Voucher voucher = voucherRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new NotFoundException("Not found voucher!", ErrorCode.VOUCHER_ERROR_NOT_FOUND));
        return makeSuccessResponse(voucherMapper.fromEntityToVoucherDto(voucher), "Get voucher success");
    }

    @PutMapping(value = "/done/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('VOU_U')")
    @Transactional
    public ApiMessageDto<Void> done(@PathVariable Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not found voucher!", ErrorCode.VOUCHER_ERROR_NOT_FOUND));

        if (AIConstant.VOUCHER_STATE_PENDING.equals(voucher.getState())) {
            voucherRepository.delete(voucher);
        } else if (AIConstant.VOUCHER_STATE_ACTIVE.equals(voucher.getState())) {
            voucher.setState(AIConstant.VOUCHER_STATE_DONE);
            voucherRepository.save(voucher);
        } else {
            throw new BadRequestException("Voucher already done", ErrorCode.VOUCHER_ERROR_ALREADY_DONE);
        }

        return makeSuccessResponse("Update voucher state success");
    }
}
