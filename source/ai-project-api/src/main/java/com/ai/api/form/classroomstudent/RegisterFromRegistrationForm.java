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
public class RegisterFromRegistrationForm {
    @NotNull(message = "registrationId can not be null")
    @Schema(name = "registrationId", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long registrationId;

    @Schema(name = "fullName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String fullName;
}
