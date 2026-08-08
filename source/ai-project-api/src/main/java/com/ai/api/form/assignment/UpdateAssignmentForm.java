package com.ai.api.form.assignment;

import com.ai.api.form.StringToLongDeserializer;
import com.ai.api.validation.AssignmentState;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Schema
public class UpdateAssignmentForm {
    @NotNull(message = "id cannot be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @NotBlank(message = "title cannot be null")
    @Schema(name = "title", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(name = "description")
    private String description;

    @Schema(name = "deadline")
    private Date deadline;

    @Schema(name = "fileAttachmentUrl")
    private String fileAttachmentUrl;

    @AssignmentState
    @NotNull(message = "state cannot be null")
    @Schema(name = "state", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer state;
}
