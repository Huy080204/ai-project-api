package com.ai.api.form.reaction;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ai.api.form.StringToLongDeserializer;
import com.ai.api.validation.ReactionTypeConstraint;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Schema
public class CreateReactionForm {
    @NotNull(message = "courseId cant not be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "courseId", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long courseId;

    @NotNull(message = "studentId cant not be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "studentId", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long studentId;

    @NotBlank(message = "content cant not be null")
    @Schema(name = "content", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @NotNull(message = "type cant not be null")
    @ReactionTypeConstraint
    @Schema(name = "type", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;
}
