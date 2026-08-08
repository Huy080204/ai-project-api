package com.ai.api.dto.classroomstudent;

import com.ai.api.dto.ABasicAdminDto;
import com.ai.api.dto.classroom.ClassroomDto;
import com.ai.api.dto.student.StudentDto;
import com.ai.api.dto.voucher.VoucherDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Schema
public class ClassroomStudentDto extends ABasicAdminDto {
    @Schema(name = "classroom")
    private ClassroomDto classroom;

    @Schema(name = "student")
    private StudentDto student;

    @Schema(name = "dateRegistration")
    private Date dateRegistration;

    @Schema(name = "dateDone")
    private Date dateDone;

    @Schema(name = "state")
    private Integer state;

    @Schema(name = "voucher")
    private VoucherDto voucher;

    @Schema(name = "discountAmount")
    private BigDecimal discountAmount;
}
