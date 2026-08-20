package com.ai.api.dto.syllabusmaterial;

import com.ai.api.dto.ABasicAdminDto;
import com.ai.api.dto.LongToStringIfWebSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class SyllabusMaterialDto extends ABasicAdminDto {
    @Schema(name = "syllabusId")
    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    private Long syllabusId;

    @Schema(name = "title")
    private String title;

    @Schema(name = "fileUrl")
    private String fileUrl;
}
