package com.ai.api.repository;

import com.ai.api.model.SyllabusMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SyllabusMaterialRepository extends JpaRepository<SyllabusMaterial, Long>, JpaSpecificationExecutor<SyllabusMaterial> {
    List<SyllabusMaterial> findBySyllabusId(Long syllabusId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SyllabusMaterial s WHERE s.syllabus.id = :syllabusId")
    void deleteAllBySyllabusId(@Param("syllabusId") Long syllabusId);

    @Query("SELECT s.fileUrl FROM SyllabusMaterial s WHERE s.syllabus.course.id = :courseId AND s.fileUrl IS NOT NULL AND s.fileUrl != ''")
    List<String> findFileUrlsBySyllabusCourseId(@Param("courseId") Long courseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SyllabusMaterial s WHERE s.syllabus.id IN (SELECT sy.id FROM Syllabus sy WHERE sy.course.id = :courseId)")
    void deleteAllBySyllabusCourseId(@Param("courseId") Long courseId);
}
