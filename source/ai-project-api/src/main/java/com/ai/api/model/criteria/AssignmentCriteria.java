package com.ai.api.model.criteria;

import com.ai.api.model.Assignment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class AssignmentCriteria {

    private Long id;
    private String title;
    private Integer state;
    private Integer status;
    private Long syllabusId;

    @Schema(hidden = true)
    public Specification<Assignment> getCriteria() {
        return new Specification<Assignment>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Assignment> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (!StringUtils.isEmpty(getTitle())) {
                    predicates.add(cb.like(cb.lower(root.get("title")), "%" + getTitle().toLowerCase() + "%"));
                }

                if (getState() != null) {
                    predicates.add(cb.equal(root.get("state"), getState()));
                }

                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }

                if (getSyllabusId() != null) {
                    Join<Assignment, Object> joinSyllabus = root.join("syllabus", JoinType.INNER);
                    predicates.add(cb.equal(joinSyllabus.get("id"), getSyllabusId()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
