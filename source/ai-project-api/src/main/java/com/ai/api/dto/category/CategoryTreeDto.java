package com.ai.api.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema
public class CategoryTreeDto extends CategoryDto {
    @Schema(name = "children")
    private List<CategoryDto> children;
}
