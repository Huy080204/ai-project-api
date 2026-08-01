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
@Table(name = DatabaseConstant.PREFIX_TABLE + "class_room")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Classroom extends Auditable<String> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    private Date startDate;

    private Date endDate;

    private Integer state = AIConstant.CLASSROOM_STATE_PENDING;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;
}
