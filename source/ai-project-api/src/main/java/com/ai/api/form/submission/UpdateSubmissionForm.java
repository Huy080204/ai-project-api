package com.ai.api.form.submission;

import com.ai.api.form.StringToLongDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema
public class UpdateSubmissionForm {
    @NotNull(message = "id cannot be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @NotNull(message = "assignmentId cannot be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "assignmentId", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long assignmentId;

    @NotNull(message = "studentId cannot be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "studentId", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long studentId;

    @Schema(name = "fileUrl")
    private String fileUrl;

    @Schema(name = "content")
    private String content;
}
