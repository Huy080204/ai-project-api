package com.ai.api.dto.reaction;

import com.ai.api.dto.course.CourseDto;
import com.ai.api.dto.student.StudentDto;
import com.ai.api.dto.ABasicAdminDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema
public class ReactionDto extends ABasicAdminDto {
    @Schema(name = "course")
    private CourseDto course;

    @Schema(name = "student")
    private StudentDto student;

    @Schema(name = "content")
    private String content;

    @Schema(name = "type")
    private Integer type;
}
