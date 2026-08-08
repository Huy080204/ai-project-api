package com.ai.api.model.criteria;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;

import com.ai.api.constant.AIConstant;
import com.ai.api.model.ClassroomStudent;
import com.ai.api.model.Student;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class StudentCriteria implements Serializable {
    private static final long serialVersionUID = 1L;

    private String address;
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private Long groupId;
    private Long ignoreClassroomId;

    @Schema(hidden = true)
    public Specification<Student> getCriteria() {
        return new Specification<Student>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Student> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getAddress() != null) {
                    predicates.add(cb.like(cb.lower(root.get("address")), "%" + getAddress().toLowerCase() + "%"));
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
                if (getIgnoreClassroomId() != null) {
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<ClassroomStudent> subRoot = subquery.from(ClassroomStudent.class);
                    subquery.select(subRoot.get("student").get("id"))
                            .where(cb.equal(subRoot.get("classroom").get("id"), getIgnoreClassroomId()));
                    predicates.add(cb.not(root.get("id").in(subquery)));
                }
                predicates.add(cb.notEqual(root.get("status"), AIConstant.STATUS_DELETE));
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
