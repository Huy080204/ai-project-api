package com.ai.api.model.criteria;

import com.ai.api.model.SyllabusMaterial;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class SyllabusMaterialCriteria implements Serializable {

    private Long syllabusId;
    private String title;

    @Schema(hidden = true)
    public Specification<SyllabusMaterial> getCriteria() {
        return new Specification<SyllabusMaterial>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<SyllabusMaterial> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getSyllabusId() != null) {
                    predicates.add(cb.equal(root.get("syllabus").get("id"), getSyllabusId()));
                }
                if (!StringUtils.isEmpty(getTitle())) {
                    predicates.add(cb.like(cb.lower(root.get("title")), "%" + getTitle().toLowerCase() + "%"));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
