package com.ai.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ai.api.constant.DatabaseConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = DatabaseConstant.PREFIX_TABLE + "account")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Account extends Auditable<String> {
    private Integer kind;

    private String username;

    private String phone;

    private String email;

    @JsonIgnore
    private String password;

    private String fullName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "avatar_path")
    private String avatarPath;

    @Column(name = "is_super_admin")
    private Boolean isSuperAdmin = false;
}
