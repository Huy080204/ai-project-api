package com.ai.api.form.classroom;

import com.ai.api.form.StringToLongDeserializer;
import com.ai.api.validation.ClassroomState;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Schema
public class ChangeClassroomStateForm {
    @NotNull(message = "id cant not be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @ClassroomState(allowNull = false)
    @Schema(name = "state", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer state;
}
