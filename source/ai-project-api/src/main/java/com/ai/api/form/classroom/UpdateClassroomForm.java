package com.ai.api.form.classroom;

import com.ai.api.form.StringToLongDeserializer;
import com.ai.api.validation.DateRange;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Schema
@DateRange(startField = "startDate", endField = "endDate")
public class UpdateClassroomForm {
    @NotNull(message = "id cant not be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @NotNull(message = "courseId cant not be null")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @Schema(name = "courseId", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long courseId;

    @NotNull(message = "startDate cant not be null")
    @Schema(name = "startDate", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date startDate;

    @NotNull(message = "endDate cant not be null")
    @Schema(name = "endDate", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date endDate;

    @NotNull(message = "price cant not be null")
    @Schema(name = "price", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;
}
