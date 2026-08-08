package com.ai.api.model.criteria;

import com.ai.api.model.Rating;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class RatingCriteria implements Serializable {
    private Long id;
    private Long courseId;
    private Long studentId;
    private Integer star;
    private Integer status;

    @Schema(hidden = true)
    public Specification<Rating> getCriteria() {
        return new Specification<Rating>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Rating> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }
                if (getCourseId() != null) {
                    predicates.add(cb.equal(root.get("course").get("id"), getCourseId()));
                }
                if (getStudentId() != null) {
                    predicates.add(cb.equal(root.get("student").get("id"), getStudentId()));
                }
                if (getStar() != null) {
                    predicates.add(cb.equal(root.get("star"), getStar()));
                }
                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
