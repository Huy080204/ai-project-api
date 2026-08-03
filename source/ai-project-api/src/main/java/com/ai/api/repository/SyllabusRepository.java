package com.ai.api.repository;

import com.ai.api.model.Syllabus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SyllabusRepository extends JpaRepository<Syllabus, Long>, JpaSpecificationExecutor<Syllabus> {
    @Query("SELECT COALESCE(SUM(s.timeline), 0) FROM Syllabus s WHERE s.course.id = :courseId AND s.kind = :kind")
    Integer sumTimelineByCourseIdAndKind(@Param("courseId") Long courseId, @Param("kind") Integer kind);

    @Query("SELECT s.avatar FROM Syllabus s WHERE s.course.id = :courseId AND s.avatar IS NOT NULL")
    List<String> findAvatarsByCourseId(@Param("courseId") Long courseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Syllabus s WHERE s.course.id = :courseId")
    void deleteAllByCourseId(@Param("courseId") Long courseId);

    List<Syllabus> findByCourseIdOrderByOrderingAsc(Long courseId);
}
