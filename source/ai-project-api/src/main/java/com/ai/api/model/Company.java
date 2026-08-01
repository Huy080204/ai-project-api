package com.ai.api.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

@Entity
@Table(name = "db_company")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Company extends Auditable<String> {
    @Column(name = "name")
    private String name;

    @Column(name = "avatar")
    private String avatar;
}
