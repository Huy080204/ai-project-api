package com.ai.api.model;

import com.ai.api.constant.AIConstant;
import com.ai.api.constant.DatabaseConstant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "voucher")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Voucher extends Auditable<String> {
    private String code;

    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    private Integer type;

    @Column(name = "value", precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "max_discount_value", precision = 10, scale = 2)
    private BigDecimal maxDiscountValue;

    @Column(name = "min_order_value", precision = 10, scale = 2)
    private BigDecimal minOrderValue;

    private Date startDate;

    private Date endDate;

    private Integer usageLimit;

    private Integer totalUsed = 0;

    private Integer state = AIConstant.VOUCHER_STATE_PENDING;
}
