package com.ai.api.form.notificationgroup;

import com.ai.api.form.StringToLongDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema
public class CreateNotificationGroupForm {
    @NotNull(message = "classroomId cannot be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "classroomId", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long classroomId;

    @NotBlank(message = "name cannot be null")
    @Schema(name = "name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(name = "avatar")
    private String avatar;

    @NotBlank(message = "zaloUrl cannot be null")
    @Schema(name = "zaloUrl", requiredMode = Schema.RequiredMode.REQUIRED)
    private String zaloUrl;
}
