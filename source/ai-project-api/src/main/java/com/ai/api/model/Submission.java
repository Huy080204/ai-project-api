package com.ai.api.model;

import com.ai.api.constant.AIConstant;
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

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "submission")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Submission extends Auditable<String> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    private String fileUrl;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    private Double score;

    private Integer state = AIConstant.SUBMISSION_STATE_PENDING;
}
