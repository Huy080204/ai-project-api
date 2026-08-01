package com.ai.api.form.classroomstudent;

import com.ai.api.form.StringToLongDeserializer;
import com.ai.api.validation.ClassroomStudentState;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Schema
public class ChangeClassroomStudentStateForm {
    @NotNull(message = "id can not be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @ClassroomStudentState(allowNull = false)
    @Schema(name = "state", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer state;
}
