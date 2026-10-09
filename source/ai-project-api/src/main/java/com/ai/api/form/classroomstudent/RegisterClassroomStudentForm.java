package com.ai.api.form.classroomstudent;

import com.ai.api.form.StringToLongDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Schema
public class RegisterClassroomStudentForm {
    @NotNull(message = "classroomId can not be null")
    @Schema(name = "classroomId", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long classroomId;

    @NotNull(message = "studentId can not be null")
    @Schema(name = "studentId", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long studentId;
}
