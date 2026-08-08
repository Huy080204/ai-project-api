package com.ai.api.model;

import com.ai.api.constant.AIConstant;
import com.ai.api.constant.DatabaseConstant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "assignment")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Assignment extends Auditable<String> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "syllabus_id")
    private Syllabus syllabus;

    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    private Date deadline;

    private String fileAttachmentUrl;

    private Integer state = AIConstant.ASSIGNMENT_STATE_DRAFT;
}
