package com.ai.api.model.criteria;

import com.ai.api.model.ClassroomStudent;
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
public class ClassroomStudentCriteria implements Serializable {
    private Long id;
    private Long classroomId;
    private Long studentId;
    private Integer state;

    public Specification<ClassroomStudent> getCriteria() {
        return new Specification<ClassroomStudent>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<ClassroomStudent> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }
                if (getClassroomId() != null) {
                    predicates.add(cb.equal(root.get("classroom").get("id"), getClassroomId()));
                }
                if (getStudentId() != null) {
                    predicates.add(cb.equal(root.get("student").get("id"), getStudentId()));
                }
                if (getState() != null) {
                    predicates.add(cb.equal(root.get("state"), getState()));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
