package com.ai.api.repository;

import com.ai.api.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long>, JpaSpecificationExecutor<Classroom> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Classroom c WHERE c.course.id = :courseId")
    void deleteAllByCourseId(@Param("courseId") Long courseId);
}
