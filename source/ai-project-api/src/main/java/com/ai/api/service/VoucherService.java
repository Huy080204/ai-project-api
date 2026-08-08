package com.ai.api.service;

import com.ai.api.constant.AIConstant;
import com.ai.api.dto.ErrorCode;
import com.ai.api.exception.BadRequestException;
import com.ai.api.exception.NotFoundException;
import com.ai.api.model.Voucher;
import com.ai.api.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@Component
public class VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    public Voucher validateAndApplyVoucher(Long voucherId, BigDecimal orderValue) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new NotFoundException("Voucher not found", ErrorCode.VOUCHER_ERROR_NOT_FOUND));

        if (!AIConstant.VOUCHER_STATE_ACTIVE.equals(voucher.getState())) {
            throw new BadRequestException("Voucher is not active", ErrorCode.VOUCHER_ERROR_INVALID);
        }

        Date now = new Date();
        if (now.before(voucher.getStartDate()) || now.after(voucher.getEndDate())) {
            throw new BadRequestException("Voucher is not within its valid time range", ErrorCode.VOUCHER_ERROR_INVALID);
        }

        if (voucher.getMinOrderValue() != null && orderValue.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new BadRequestException("Order value does not meet voucher minimum order value", ErrorCode.VOUCHER_ERROR_INVALID);
        }

        if (voucher.getUsageLimit() != null && voucher.getTotalUsed() >= voucher.getUsageLimit()) {
            throw new BadRequestException("Voucher usage limit reached", ErrorCode.VOUCHER_ERROR_INVALID);
        }

        voucher.setTotalUsed(voucher.getTotalUsed() + 1);
        voucherRepository.save(voucher);

        return voucher;
    }

    public BigDecimal calculateDiscountAmount(Voucher voucher, BigDecimal orderValue) {
        if (AIConstant.VOUCHER_TYPE_PERCENT.equals(voucher.getType())) {
            BigDecimal discountAmount = orderValue.multiply(voucher.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (voucher.getMaxDiscountValue() != null && discountAmount.compareTo(voucher.getMaxDiscountValue()) > 0) {
                return voucher.getMaxDiscountValue();
            }
            return discountAmount;
        }
        return voucher.getValue().min(orderValue);
    }
}
