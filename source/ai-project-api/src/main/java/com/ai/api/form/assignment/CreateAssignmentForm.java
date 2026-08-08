package com.ai.api.form.assignment;

import com.ai.api.form.StringToLongDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Schema
public class CreateAssignmentForm {
    @NotNull(message = "syllabusId cannot be null")
    @Schema(name = "syllabusId", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long syllabusId;

    @NotBlank(message = "title cannot be null")
    @Schema(name = "title", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(name = "description")
    private String description;

    @Schema(name = "deadline")
    private Date deadline;

    @Schema(name = "fileAttachmentUrl")
    private String fileAttachmentUrl;
}
