package com.ai.api.form.registration;

import com.ai.api.form.StringToLongDeserializer;
import com.ai.api.validation.EmailConstraint;
import com.ai.api.validation.PhoneConstraint;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Schema
public class CreateRegistrationForm {
    @NotNull(message = "classroomId cant not be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "classroomId", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long classroomId;

    @NotBlank(message = "fullName cant not be null")
    @Schema(name = "fullName", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @NotBlank(message = "email cant not be null")
    @EmailConstraint
    @Schema(name = "email", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "phone cant not be null")
    @PhoneConstraint
    @Schema(name = "phone", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @Schema(name = "message")
    private String message;
}
