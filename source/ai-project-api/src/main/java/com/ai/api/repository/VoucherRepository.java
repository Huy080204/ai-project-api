package com.ai.api.repository;

import com.ai.api.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long>, JpaSpecificationExecutor<Voucher> {
    boolean existsByCodeIgnoreCase(String code);

    Optional<Voucher> findByCodeIgnoreCase(String code);

    @Modifying
    @Transactional
    @Query("UPDATE Voucher v SET v.state = :doneState WHERE v.state = :activeState AND v.endDate < :now")
    void expireActiveVouchers(@Param("activeState") Integer activeState,
                               @Param("doneState") Integer doneState,
                               @Param("now") Date now);
}
