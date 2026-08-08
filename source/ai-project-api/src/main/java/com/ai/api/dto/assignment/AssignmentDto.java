package com.ai.api.dto.assignment;

import com.ai.api.dto.ABasicAdminDto;
import com.ai.api.dto.LongToStringIfWebSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema
public class AssignmentDto extends ABasicAdminDto {
    @Schema(name = "syllabusId")
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    private Long syllabusId;

    @Schema(name = "title")
    private String title;

    @Schema(name = "description")
    private String description;

    @Schema(name = "deadline")
    private Date deadline;

    @Schema(name = "fileAttachmentUrl")
    private String fileAttachmentUrl;

    @Schema(name = "state")
    private Integer state;
}
