package com.ai.api.form.mentor;

import com.ai.api.form.StringToLongDeserializer;
import com.ai.api.validation.EmailConstraint;
import com.ai.api.validation.PhoneConstraint;
import com.ai.api.validation.UsernameConstraint;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Schema
public class CreateMentorForm {
    @UsernameConstraint
    @Schema(name = "username", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @EmailConstraint
    @Schema(name = "email", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @PhoneConstraint
    @Schema(name = "phone", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @NotEmpty(message = "password cant not be null")
    @Schema(name = "password", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "fullName cant not be null")
    @Schema(name = "fullName", example = "Nguyen Van A", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @Schema(name = "avatarPath")
    private String avatarPath;

    @NotNull(message = "groupId cant not be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "groupId", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long groupId;

    @NotBlank(message = "position cant not be null")
    @Schema(name = "position", requiredMode = Schema.RequiredMode.REQUIRED)
    private String position;

    @NotBlank(message = "description cant not be null")
    @Schema(name = "description", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;
}
