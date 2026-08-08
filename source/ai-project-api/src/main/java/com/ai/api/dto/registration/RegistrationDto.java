package com.ai.api.dto.registration;

import com.ai.api.dto.ABasicAdminDto;
import com.ai.api.dto.classroom.ClassroomDto;
import com.ai.api.dto.student.StudentDto;
import com.ai.api.dto.voucher.VoucherDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema
public class RegistrationDto extends ABasicAdminDto {
    @Schema(name = "classroom")
    private ClassroomDto classroom;
    @Schema(name = "student")
    private StudentDto student;
    @Schema(name = "fullName")
    private String fullName;
    @Schema(name = "email")
    private String email;
    @Schema(name = "phone")
    private String phone;
    @Schema(name = "message")
    private String message;
    @Schema(name = "voucher")
    private VoucherDto voucher;
    @Schema(name = "discountAmount")
    private BigDecimal discountAmount;
}
