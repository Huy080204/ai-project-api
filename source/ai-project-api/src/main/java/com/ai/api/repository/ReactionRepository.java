package com.ai.api.repository;

import com.ai.api.model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ReactionRepository extends JpaRepository<Reaction, Long>, JpaSpecificationExecutor<Reaction> {
    Reaction findByCourseIdAndStudentIdAndStatus(Long courseId, Long studentId, Integer status);

    @Modifying
    @Transactional
    @Query("DELETE FROM Reaction r WHERE r.course.id = :courseId")
    void deleteAllByCourseId(@Param("courseId") Long courseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Reaction r WHERE r.student.id = :studentId")
    void deleteAllByStudentId(@Param("studentId") Long studentId);
}
