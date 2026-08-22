package com.ai.api.model;

import com.ai.api.constant.DatabaseConstant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "notification_group")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class NotificationGroup extends Auditable<String> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    private String name;

    private String avatar;

    private String zaloUrl;

    private Integer ordering = 0;
}
