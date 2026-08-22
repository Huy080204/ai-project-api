package com.ai.api.dto.notificationgroup;

import com.ai.api.dto.ABasicAdminDto;
import com.ai.api.dto.classroom.ClassroomDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class NotificationGroupDto extends ABasicAdminDto {
    @Schema(name = "classroom")
    private ClassroomDto classroom;

    @Schema(name = "groupName")
    private String groupName;

    @Schema(name = "avatar")
    private String avatar;

    @Schema(name = "zaloUrl")
    private String zaloUrl;
}
