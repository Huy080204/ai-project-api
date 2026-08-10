package com.ai.api.form.jobposting;

import com.ai.api.form.StringToLongDeserializer;
import com.ai.api.validation.JobPostingState;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Schema
public class UpdateJobPostingStateForm {
    @NotNull(message = "id can not be null")
    @Schema(name = "id", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long id;

    @JobPostingState(allowNull = false)
    @NotNull(message = "state can not be null")
    @Schema(name = "state", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer state;
}
