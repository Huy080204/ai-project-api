package com.ai.api.model;

import com.ai.api.constant.DatabaseConstant;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "news")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class News extends Auditable<String> {
    private String title;

    private String avatar;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}
