package com.ai.api.form.classroomstudent;

import com.ai.api.form.StringToLongDeserializer;
import com.ai.api.validation.EmailConstraint;
import com.ai.api.validation.PhoneConstraint;
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

    @EmailConstraint(allowNull = true)
    @Schema(name = "email", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String email;

    @PhoneConstraint(allowNull = true)
    @Schema(name = "phone", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String phone;
}
