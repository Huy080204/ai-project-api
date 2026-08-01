package com.ai.api.dto.report;

import com.ai.api.dto.LongToStringIfWebSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class StudentReportDto {
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    @Schema(name = "id")
    private Long id;

    @Schema(name = "fullName")
    private String fullName;

    @Schema(name = "email")
    private String email;

    @Schema(name = "phone")
    private String phone;

    @Schema(name = "avatarPath")
    private String avatarPath;

    @Schema(name = "totalEnrolledClasses")
    private Long totalEnrolledClasses;
}
