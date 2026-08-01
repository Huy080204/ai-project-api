package com.ai.api.form.student;

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
public class UpdateStudentForm {
    @NotNull(message = "id cant not be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(name = "address")
    private String address;

    @Schema(name = "fullName")
    private String fullName;

    @Schema(name = "avatarPath")
    private String avatarPath;

    @EmailConstraint
    @Schema(name = "email", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @PhoneConstraint
    @Schema(name = "phone", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;
}
