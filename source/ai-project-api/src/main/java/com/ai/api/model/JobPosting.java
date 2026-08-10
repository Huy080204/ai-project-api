package com.ai.api.model;

import com.ai.api.constant.DatabaseConstant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "job_posting")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class JobPosting extends Auditable<String> {
    @Column(name = "position")
    private String position;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "salary", precision = 10, scale = 2)
    private BigDecimal salary;

    @Column(name = "state")
    private Integer state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
}
