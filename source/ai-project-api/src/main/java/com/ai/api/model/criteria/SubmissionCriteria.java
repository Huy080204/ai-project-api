package com.ai.api.model.criteria;

import com.ai.api.model.Submission;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Data
public class SubmissionCriteria {

    private Long id;
    private Long assignmentId;
    private Long studentId;
    private Integer state;
    private Integer status;

    @Schema(hidden = true)
    public Specification<Submission> getCriteria() {
        return new Specification<Submission>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Submission> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (getAssignmentId() != null) {
                    Join<Submission, Object> joinAssignment = root.join("assignment", JoinType.INNER);
                    predicates.add(cb.equal(joinAssignment.get("id"), getAssignmentId()));
                }

                if (getStudentId() != null) {
                    Join<Submission, Object> joinStudent = root.join("student", JoinType.INNER);
                    predicates.add(cb.equal(joinStudent.get("id"), getStudentId()));
                }

                if (getState() != null) {
                    predicates.add(cb.equal(root.get("state"), getState()));
                }

                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
