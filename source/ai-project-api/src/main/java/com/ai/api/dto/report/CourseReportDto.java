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
public class CourseReportDto {
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    @Schema(name = "id")
    private Long id;

    @Schema(name = "name")
    private String name;

    @Schema(name = "avatar")
    private String avatar;

    @Schema(name = "totalStudents")
    private Long totalStudents;
}
