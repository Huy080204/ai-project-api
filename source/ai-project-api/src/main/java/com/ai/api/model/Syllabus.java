package com.ai.api.model;

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

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "syllabus")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Syllabus extends Auditable<String> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    private Integer kind;

    private String name;

    private String avatar;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    private Integer ordering;

    private Integer timeline = 0;
}
