package com.ai.api.dto.mentor;

import com.ai.api.dto.ABasicAdminDto;
import com.ai.api.dto.account.AccountDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema
public class MentorDto extends ABasicAdminDto {
    @Schema(name = "position")
    private String position;

    @Schema(name = "description")
    private String description;

    @Schema(name = "account")
    private AccountDto account;
}
