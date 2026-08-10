package com.ai.api.model;

import com.ai.api.constant.AIConstant;
import com.ai.api.constant.DatabaseConstant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "reaction")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Reaction extends Auditable<String> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    private Integer type;
}
