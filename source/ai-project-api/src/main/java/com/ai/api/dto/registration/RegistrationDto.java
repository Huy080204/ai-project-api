package com.ai.api.dto.registration;

import com.ai.api.dto.ABasicAdminDto;
import com.ai.api.dto.classroom.ClassroomDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema
public class RegistrationDto extends ABasicAdminDto {
    @Schema(name = "classroom")
    private ClassroomDto classroom;
    @Schema(name = "fullName")
    private String fullName;
    @Schema(name = "email")
    private String email;
    @Schema(name = "phone")
    private String phone;
    @Schema(name = "message")
    private String message;
}
