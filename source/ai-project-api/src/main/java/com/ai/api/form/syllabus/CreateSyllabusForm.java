package com.ai.api.form.syllabus;

import com.ai.api.form.StringToLongDeserializer;
import com.ai.api.validation.SyllabusKind;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Schema
public class CreateSyllabusForm {
    @NotNull(message = "courseId cant not be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "courseId", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long courseId;

    @SyllabusKind
    @Schema(name = "kind", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer kind;

    @NotBlank(message = "name cant not be null")
    @Schema(name = "name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "avatar cant not be null")
    @Schema(name = "avatar", requiredMode = Schema.RequiredMode.REQUIRED)
    private String avatar;

    @NotBlank(message = "description cant not be null")
    @Schema(name = "description", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @NotNull(message = "ordering cant not be null")
    @Schema(name = "ordering", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer ordering;

    @Min(value = 0, message = "timeline must be greater than or equal to 0")
    @Schema(name = "timeline")
    private Integer timeline;
}
