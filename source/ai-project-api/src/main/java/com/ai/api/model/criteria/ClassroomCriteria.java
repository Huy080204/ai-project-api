package com.ai.api.model.criteria;

import com.ai.api.model.Classroom;
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
public class ClassroomCriteria implements Serializable {
    private Long id;
    private Long courseId;
    private Integer state;

    @Schema(hidden = true)
    public Specification<Classroom> getSpecification() {
        return new Specification<Classroom>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Classroom> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }
                if (getCourseId() != null) {
                    predicates.add(cb.equal(root.get("course").get("id"), getCourseId()));
                }
                if (getState() != null) {
                    predicates.add(cb.equal(root.get("state"), getState()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
