package com.ai.api.form.company;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Schema
public class CreateCompanyForm {
    @NotBlank(message = "name cant not be null")
    @Schema(name = "name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "avatar cant not be null")
    @Schema(name = "avatar", requiredMode = Schema.RequiredMode.REQUIRED)
    private String avatar;
}
