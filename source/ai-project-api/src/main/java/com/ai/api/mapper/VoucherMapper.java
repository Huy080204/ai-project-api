package com.ai.api.mapper;

import com.ai.api.dto.voucher.VoucherDto;
import com.ai.api.form.voucher.CreateVoucherForm;
import com.ai.api.form.voucher.UpdateVoucherForm;
import com.ai.api.model.Voucher;
import org.mapstruct.BeanMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VoucherMapper {

    @Mapping(source = "code", target = "code")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "value", target = "value")
    @Mapping(source = "maxDiscountValue", target = "maxDiscountValue")
    @Mapping(source = "minOrderValue", target = "minOrderValue")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "usageLimit", target = "usageLimit")
    @BeanMapping(ignoreByDefault = true)
    Voucher fromCreateFormToEntity(CreateVoucherForm createVoucherForm);

    @Mapping(source = "code", target = "code")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "value", target = "value")
    @Mapping(source = "maxDiscountValue", target = "maxDiscountValue")
    @Mapping(source = "minOrderValue", target = "minOrderValue")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "usageLimit", target = "usageLimit")
    @BeanMapping(ignoreByDefault = true)
    void updateEntityFromForm(UpdateVoucherForm updateVoucherForm, @MappingTarget Voucher voucher);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "code", target = "code")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "value", target = "value")
    @Mapping(source = "maxDiscountValue", target = "maxDiscountValue")
    @Mapping(source = "minOrderValue", target = "minOrderValue")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "usageLimit", target = "usageLimit")
    @Mapping(source = "totalUsed", target = "totalUsed")
    @Mapping(source = "state", target = "state")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "modifiedDate", target = "modifiedDate")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToVoucherDto")
    VoucherDto fromEntityToVoucherDto(Voucher voucher);

    @IterableMapping(elementTargetType = VoucherDto.class, qualifiedByName = "fromEntityToVoucherDto")
    List<VoucherDto> fromEntityListToVoucherDtoList(List<Voucher> vouchers);

    @Mapping(source = "id", target = "id")
    @BeanMapping(ignoreByDefault = true)
    @Named("fromEntityToVoucherIdDto")
    VoucherDto fromEntityToVoucherIdDto(Voucher voucher);
}
