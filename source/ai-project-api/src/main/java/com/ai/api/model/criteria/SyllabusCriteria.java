package com.ai.api.model.criteria;

import com.ai.api.model.Syllabus;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class SyllabusCriteria implements Serializable {
    private Long id;
    private Long courseId;
    private Integer kind;
    private String name;
    private Integer status;

    public Specification<Syllabus> getSpecification() {
        return new Specification<Syllabus>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Syllabus> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }
                if (getCourseId() != null) {
                    predicates.add(cb.equal(root.get("course").get("id"), getCourseId()));
                }
                if (getKind() != null) {
                    predicates.add(cb.equal(root.get("kind"), getKind()));
                }
                if (getName() != null) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + getName().toLowerCase() + "%"));
                }
                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
