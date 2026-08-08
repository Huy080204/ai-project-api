package com.ai.api.scheduler;

import com.ai.api.constant.AIConstant;
import com.ai.api.repository.VoucherRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class VoucherScheduler {

    @Autowired
    private VoucherRepository voucherRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
    public void expireActiveVouchers() {
        log.info("Running voucher auto-expire scheduler at {}", new Date());
        voucherRepository.expireActiveVouchers(
                AIConstant.VOUCHER_STATE_ACTIVE,
                AIConstant.VOUCHER_STATE_DONE,
                new Date());
    }
}
