package com.ai.api.dto.news;

import com.ai.api.dto.ABasicAdminDto;
import com.ai.api.dto.LongToStringIfWebSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema
public class NewsDto extends ABasicAdminDto {
    @Schema(name = "title")
    private String title;

    @Schema(name = "avatar")
    private String avatar;

    @Schema(name = "shortDescription")
    private String shortDescription;

    @Schema(name = "content")
    private String content;

    @JsonSerialize(using = LongToStringIfWebSerializer.class)
    @Schema(name = "categoryId")
    private Long categoryId;
}
