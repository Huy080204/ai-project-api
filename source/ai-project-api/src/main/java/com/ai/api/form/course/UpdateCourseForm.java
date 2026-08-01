package com.ai.api.form.course;

import com.ai.api.form.StringToLongDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@Schema
public class UpdateCourseForm {
    @NotNull(message = "id cant not be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @NotBlank(message = "name cant not be null")
    @Schema(name = "name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "avatar cant not be null")
    @Schema(name = "avatar", requiredMode = Schema.RequiredMode.REQUIRED)
    private String avatar;

    @NotNull(message = "price cant not be null")
    @Schema(name = "price", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @NotBlank(message = "shortDescription cant not be null")
    @Schema(name = "shortDescription", requiredMode = Schema.RequiredMode.REQUIRED)
    private String shortDescription;
}
