package com.ai.api.form.rating;

import com.ai.api.form.StringToLongDeserializer;
import com.ai.api.validation.RatingStar;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Schema
public class UpdateRatingForm {
    @NotNull(message = "id can not be null")
    @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long id;

    @NotBlank(message = "message can not be null")
    @Schema(name = "message", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @RatingStar
    @Schema(name = "star", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer star;
}
