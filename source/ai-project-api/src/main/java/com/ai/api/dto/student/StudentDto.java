package com.ai.api.dto.student;

import com.ai.api.dto.ABasicAdminDto;
import com.ai.api.dto.account.AccountDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema
public class StudentDto extends ABasicAdminDto {
    @Schema(name = "address")
    private String address;

    @Schema(name = "account")
    private AccountDto account;
}
