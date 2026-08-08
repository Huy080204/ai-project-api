package com.ai.api.form.submission;

import com.ai.api.form.StringToLongDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema
public class GradeSubmissionForm {
    @NotNull(message = "id cannot be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @NotNull(message = "score cannot be null")
    @Schema(name = "score", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double score;

    @Schema(name = "feedback")
    private String feedback;
}
