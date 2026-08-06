package com.ai.api.dto.tag;

import com.ai.api.dto.ABasicAdminDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class TagDto extends ABasicAdminDto {
    @Schema(name = "name")
    private String name;
    @Schema(name = "colorCode")
    private String colorCode;
}
