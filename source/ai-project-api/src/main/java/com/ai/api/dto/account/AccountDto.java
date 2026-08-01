package com.ai.api.dto.account;

import com.ai.api.dto.ABasicAdminDto;
import com.ai.api.dto.group.GroupDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema
public class AccountDto extends ABasicAdminDto {
    @Schema(name = "kind")
    private int kind;
    @Schema(name = "username")
    private String username;
    @Schema(name = "phone")
    private String phone;
    @Schema(name = "email")
    private String email;
    @Schema(name = "fullName")
    private String fullName;
    @Schema(name = "group")
    private GroupDto group;
    @Schema(name = "avatarPath")
    private String avatarPath;
    @Schema(name = "isSuperAdmin")
    private Boolean isSuperAdmin;
}
