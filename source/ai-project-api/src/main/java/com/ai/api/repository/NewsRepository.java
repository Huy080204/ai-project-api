package com.ai.api.repository;

import com.ai.api.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long>, JpaSpecificationExecutor<News> {
    @Query("SELECT n.avatar FROM News n WHERE n.category.id IN :categoryIds AND n.avatar IS NOT NULL")
    List<String> findAvatarsByCategoryIdIn(@Param("categoryIds") List<Long> categoryIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM News n WHERE n.category.id IN :categoryIds")
    void deleteAllByCategoryIdIn(@Param("categoryIds") List<Long> categoryIds);
}
