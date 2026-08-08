package com.ai.api.model.criteria;

import com.ai.api.model.Category;
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
public class CategoryCriteria implements Serializable {
    private Long id;
    private String name;
    private Long parentId;
    private Integer status;
    private Integer kind;
    private Boolean isParent;

    @Schema(hidden = true)
    public Specification<Category> getCriteria() {
        return new Specification<Category>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Category> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }
                if (getName() != null) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + getName().toLowerCase() + "%"));
                }
                if (getParentId() != null) {
                    predicates.add(cb.equal(root.get("parent").get("id"), getParentId()));
                }
                if (getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), getStatus()));
                }
                if (getKind() != null) {
                    predicates.add(cb.equal(root.get("kind"), getKind()));
                }
                if (getIsParent() != null) {
                    if (getIsParent()) {
                        predicates.add(cb.isNull(root.get("parent")));
                    } else {
                        predicates.add(cb.isNotNull(root.get("parent")));
                    }
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
