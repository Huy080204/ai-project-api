package com.ai.api.dto.company;

import com.ai.api.dto.ABasicAdminDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema
public class CompanyDto extends ABasicAdminDto {
    @Schema(name = "name")
    private String name;

    @Schema(name = "avatar")
    private String avatar;
}
