package com.ai.api.form.jobposting;

import com.ai.api.form.StringToLongDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@Schema
public class CreateJobPostingForm {
    @NotBlank(message = "position cant not be null")
    @Schema(name = "position", requiredMode = Schema.RequiredMode.REQUIRED)
    private String position;

    @NotBlank(message = "description cant not be null")
    @Schema(name = "description", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @NotNull(message = "salary cant not be null")
    @Schema(name = "salary", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal salary;

    @NotNull(message = "companyId cant not be null")
    @Schema(name = "companyId", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long companyId;
}
