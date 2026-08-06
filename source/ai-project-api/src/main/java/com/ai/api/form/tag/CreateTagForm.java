package com.ai.api.form.tag;

import com.ai.api.constant.AIConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Schema
public class CreateTagForm {
    @NotBlank(message = "name cannot be null")
    @Size(max = AIConstant.TAG_NAME_MAX_LENGTH)
    @Schema(name = "name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = AIConstant.TAG_COLOR_CODE_MAX_LENGTH)
    @Schema(name = "colorCode")
    private String colorCode;
}
