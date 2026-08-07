package com.ai.api.dto.voucher;

import com.ai.api.dto.ABasicAdminDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema
public class VoucherDto extends ABasicAdminDto {
    @Schema(name = "code")
    private String code;

    @Schema(name = "name")
    private String name;

    @Schema(name = "description")
    private String description;

    @Schema(name = "type")
    private Integer type;

    @Schema(name = "value")
    private BigDecimal value;

    @Schema(name = "maxDiscountValue")
    private BigDecimal maxDiscountValue;

    @Schema(name = "minOrderValue")
    private BigDecimal minOrderValue;

    @Schema(name = "startDate")
    private Date startDate;

    @Schema(name = "endDate")
    private Date endDate;

    @Schema(name = "usageLimit")
    private Integer usageLimit;

    @Schema(name = "totalUsed")
    private Integer totalUsed;

    @Schema(name = "state")
    private Integer state;
}
