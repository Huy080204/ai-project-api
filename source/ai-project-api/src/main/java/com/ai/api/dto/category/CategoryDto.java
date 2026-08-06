package com.ai.api.dto.category;

import com.ai.api.dto.ABasicAdminDto;
import com.ai.api.dto.LongToStringIfWebSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema
public class CategoryDto extends ABasicAdminDto {
    @Schema(name = "name")
    private String name;

    @Schema(name = "description")
    private String description;

    @Schema(name = "avatar")
    private String avatar;

    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    @Schema(name = "parentId")
    private Long parentId;
}
