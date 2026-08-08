package com.ai.api.model.criteria;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.ai.api.constant.AIConstant;
import com.ai.api.model.Mentor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MentorCriteria implements Serializable {
    private static final long serialVersionUID = 1L;

    private String position;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private Long groupId;
    private Integer status;

    @Schema(hidden = true)
    public Specification<Mentor> getCriteria() {
        return new Specification<Mentor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Mentor> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getPosition() != null) {
                    predicates.add(cb.like(cb.lower(root.get("position")), "%" + getPosition().toLowerCase() + "%"));
                }
                if (getUsername() != null) {
                    predicates.add(cb.like(cb.lower(root.get("account").get("username")),
                            "%" + getUsername().toLowerCase() + "%"));
                }
                if (getEmail() != null) {
                    predicates.add(cb.like(cb.lower(root.get("account").get("email")),
                            "%" + getEmail().toLowerCase() + "%"));
                }
                if (getPhone() != null) {
                    predicates.add(cb.like(cb.lower(root.get("account").get("phone")),
                            "%" + getPhone().toLowerCase() + "%"));
                }
                if (getFullName() != null) {
                    predicates.add(cb.like(cb.lower(root.get("account").get("fullName")),
                            "%" + getFullName().toLowerCase() + "%"));
                }
                if (getGroupId() != null) {
                    predicates.add(cb.equal(root.get("account").get("group").get("id"), getGroupId()));
                }
                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                } else {
                    predicates.add(cb.notEqual(root.get("status"), AIConstant.STATUS_DELETE));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
