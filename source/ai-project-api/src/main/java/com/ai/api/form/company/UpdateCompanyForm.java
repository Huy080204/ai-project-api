package com.ai.api.form.company;

import com.ai.api.form.StringToLongDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Schema
public class UpdateCompanyForm {
    @NotNull(message = "id can not be null")
    @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long id;

    @NotBlank(message = "name cant not be null")
    @Schema(name = "name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "avatar cant not be null")
    @Schema(name = "avatar", requiredMode = Schema.RequiredMode.REQUIRED)
    private String avatar;
}
