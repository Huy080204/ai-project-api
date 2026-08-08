package com.ai.api.model.criteria;

import com.ai.api.model.Voucher;
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
public class VoucherCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String code;
    private String name;
    private Integer type;
    private Integer state;

    @Schema(hidden = true)
    public Specification<Voucher> getCriteria() {
        return new Specification<Voucher>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Voucher> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();

                if (getId() != null) {
                    predicates.add(cb.equal(root.get("id"), getId()));
                }

                if (!StringUtils.isEmpty(getCode())) {
                    predicates.add(cb.like(cb.lower(root.get("code")), "%" + getCode().toLowerCase() + "%"));
                }

                if (!StringUtils.isEmpty(getName())) {
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + getName().toLowerCase() + "%"));
                }

                if (getType() != null) {
                    predicates.add(cb.equal(root.get("type"), getType()));
                }

                if (getState() != null) {
                    predicates.add(cb.equal(root.get("state"), getState()));
                }

                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
