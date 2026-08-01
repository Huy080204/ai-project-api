package com.ai.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.ai.api.constant.DatabaseConstant;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "mentor")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Mentor extends Auditable<String> {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    @MapsId
    private Account account;

    @Column(name = "position")
    private String position;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
