package com.ai.api.dto.rating;

import com.ai.api.dto.ABasicAdminDto;
import com.ai.api.dto.course.CourseDto;
import com.ai.api.dto.student.StudentDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema
public class RatingDto extends ABasicAdminDto {
    @Schema(name = "course")
    private CourseDto course;

    @Schema(name = "student")
    private StudentDto student;

    @Schema(name = "message")
    private String message;

    @Schema(name = "star")
    private Integer star;
}
