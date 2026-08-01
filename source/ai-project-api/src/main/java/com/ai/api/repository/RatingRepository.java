package com.ai.api.repository;

import com.ai.api.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RatingRepository extends JpaRepository<Rating, Long>, JpaSpecificationExecutor<Rating> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Rating r WHERE r.course.id = :courseId")
    void deleteAllByCourseId(@Param("courseId") Long courseId);
}
