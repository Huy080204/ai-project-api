package com.ai.api.dto.syllabus;

import com.ai.api.dto.ABasicAdminDto;
import com.ai.api.dto.course.CourseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema
public class SyllabusDto extends ABasicAdminDto {
    @Schema(name = "course")
    private CourseDto course;
    @Schema(name = "kind")
    private Integer kind;
    @Schema(name = "name")
    private String name;
    @Schema(name = "avatar")
    private String avatar;
    @Schema(name = "description")
    private String description;
    @Schema(name = "ordering")
    private Integer ordering;
    @Schema(name = "timeline")
    private Integer timeline;
}
