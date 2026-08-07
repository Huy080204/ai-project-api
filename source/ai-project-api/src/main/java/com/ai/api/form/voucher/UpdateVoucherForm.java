package com.ai.api.form.voucher;

import com.ai.api.form.StringToLongDeserializer;
import com.ai.api.validation.DateRange;
import com.ai.api.validation.VoucherType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema
@DateRange(startField = "startDate", endField = "endDate")
public class UpdateVoucherForm {
    @NotNull(message = "id cannot be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @NotBlank(message = "code cannot be null")
    @Schema(name = "code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotBlank(message = "name cannot be null")
    @Schema(name = "name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(name = "description")
    private String description;

    @NotNull(message = "type cannot be null")
    @VoucherType
    @Schema(name = "type", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;

    @NotNull(message = "value cannot be null")
    @Schema(name = "value", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal value;

    @Schema(name = "maxDiscountValue")
    private BigDecimal maxDiscountValue;

    @Schema(name = "minOrderValue")
    private BigDecimal minOrderValue;

    @NotNull(message = "startDate cannot be null")
    @Schema(name = "startDate", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date startDate;

    @NotNull(message = "endDate cannot be null")
    @Schema(name = "endDate", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date endDate;

    @Schema(name = "usageLimit")
    private Integer usageLimit;
}
