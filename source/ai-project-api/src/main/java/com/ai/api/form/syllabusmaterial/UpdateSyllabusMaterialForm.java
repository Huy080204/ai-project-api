package com.ai.api.form.syllabusmaterial;

import com.ai.api.form.StringToLongDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema
public class UpdateSyllabusMaterialForm {
    @NotNull(message = "id cannot be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @NotBlank(message = "title cannot be null")
    @Schema(name = "title", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "fileUrl cannot be null")
    @Schema(name = "fileUrl", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileUrl;
}
