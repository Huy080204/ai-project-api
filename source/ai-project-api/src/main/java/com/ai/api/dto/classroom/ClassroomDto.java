package com.ai.api.dto.classroom;

import com.ai.api.dto.ABasicAdminDto;
import com.ai.api.dto.course.CourseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Schema
public class ClassroomDto extends ABasicAdminDto {
    @Schema(name = "course")
    private CourseDto course;
    @Schema(name = "startDate")
    private Date startDate;
    @Schema(name = "endDate")
    private Date endDate;
    @Schema(name = "state")
    private Integer state;
    @Schema(name = "price")
    private BigDecimal price;
}
