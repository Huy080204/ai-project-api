package com.ai.api.form.news;

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
public class UpdateNewsForm {
    @NotNull(message = "id cant not be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @NotBlank(message = "title cant not be null")
    @Schema(name = "title", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(name = "avatar")
    private String avatar;

    @Schema(name = "shortDescription")
    private String shortDescription;

    @Schema(name = "content")
    private String content;

    @NotNull(message = "categoryId cant not be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "categoryId", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;
}
