package com.ai.api.dto.course;

import com.ai.api.dto.ABasicAdminDto;
import com.ai.api.dto.syllabus.SyllabusDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Schema
public class CourseDto extends ABasicAdminDto {
    @Schema(name = "name")
    private String name;
    @Schema(name = "avatar")
    private String avatar;
    @Schema(name = "price")
    private BigDecimal price;
    @Schema(name = "shortDescription")
    private String shortDescription;
    @Schema(name = "totalTimeline")
    private Integer totalTimeline;
    @Schema(name = "syllabuses")
    private List<SyllabusDto> syllabuses;
}
