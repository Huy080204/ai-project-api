package com.ai.api.model;

import com.ai.api.constant.DatabaseConstant;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "course")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Course extends Auditable<String> {
    private String name;

    private String avatar;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;

    private Integer totalTimeline = 0;
}
