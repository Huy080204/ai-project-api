package com.ai.api.model.criteria;

import com.ai.api.model.JobPosting;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class JobPostingCriteria implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String position;
    private Long companyId;
    private Integer state;

    @Schema(hidden = true)
    public Specification<JobPosting> getCriteria() {
        return new Specification<JobPosting>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<JobPosting> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();

                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (!StringUtils.isEmpty(getPosition())) {
                    predicates.add(cb.like(cb.lower(root.get("position")), "%" + getPosition().toLowerCase() + "%"));
                }

                if (getCompanyId() != null) {
                    predicates.add(cb.equal(root.get("company").get("id"), getCompanyId()));
                }

                if (getState() != null) {
                    predicates.add(cb.equal(root.get("state"), getState()));
                }

                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
