package com.ai.api.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

@Entity
@Table(name = "db_tag")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Tag extends Auditable<String> {
    private String name;
    private String colorCode;
}
