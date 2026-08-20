package com.ai.api.dto.jobposting;

import com.ai.api.dto.ABasicAdminDto;
import com.ai.api.dto.LongToStringIfWebSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema
public class JobPostingDto extends ABasicAdminDto {
    @Schema(name = "position")
    private String position;

    @Schema(name = "description")
    private String description;

    @Schema(name = "salary")
    private BigDecimal salary;

    @Schema(name = "state")
    private Integer state;

    @Schema(name = "companyId")
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    private Long companyId;
}
