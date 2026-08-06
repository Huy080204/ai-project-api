package com.ai.api.repository;

import com.ai.api.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    boolean existsByNameAndParentIsNull(String name);

    boolean existsByNameAndParentIsNullAndIdNot(String name, Long id);

    boolean existsByNameAndParentId(String name, Long parentId);

    boolean existsByNameAndParentIdAndIdNot(String name, Long parentId, Long id);

    List<Category> findByParentIdIn(List<Long> parentIds);
}
