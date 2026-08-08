package com.ai.api.dto.submission;

import com.ai.api.dto.ABasicAdminDto;
import com.ai.api.dto.LongToStringIfWebSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class SubmissionDto extends ABasicAdminDto {
    @Schema(name = "assignmentId")
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    private Long assignmentId;

    @Schema(name = "studentId")
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    private Long studentId;

    @Schema(name = "fileUrl")
    private String fileUrl;

    @Schema(name = "content")
    private String content;

    @Schema(name = "feedback")
    private String feedback;

    @Schema(name = "score")
    private Double score;

    @Schema(name = "state")
    private Integer state;
}
