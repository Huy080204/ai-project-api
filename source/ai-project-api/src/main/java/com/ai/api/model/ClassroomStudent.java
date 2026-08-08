package com.ai.api.model;

import com.ai.api.constant.AIConstant;
import com.ai.api.constant.DatabaseConstant;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "classroom_student")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ClassroomStudent extends Auditable<String> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    private Date dateRegistration;

    private Date dateDone;

    private Integer state = AIConstant.CLASSROOM_STUDENT_STATE_PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;
}
