package com.ai.api.repository;

import com.ai.api.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long>, JpaSpecificationExecutor<Assignment> {

    /**
     * Find all file attachment URLs for assignments under a given syllabus.
     */
    @Query("SELECT a.fileAttachmentUrl FROM Assignment a WHERE a.syllabus.id = :syllabusId AND a.fileAttachmentUrl IS NOT NULL AND a.fileAttachmentUrl != ''")
    List<String> findFileAttachmentUrlsBySyllabusId(@Param("syllabusId") Long syllabusId);

    /**
     * Delete all assignments under a given syllabus.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Assignment a WHERE a.syllabus.id = :syllabusId")
    void deleteAllBySyllabusId(@Param("syllabusId") Long syllabusId);

    /**
     * Find all file attachment URLs for assignments under a given course (via syllabus).
     */
    @Query("SELECT a.fileAttachmentUrl FROM Assignment a WHERE a.syllabus.course.id = :courseId AND a.fileAttachmentUrl IS NOT NULL AND a.fileAttachmentUrl != ''")
    List<String> findFileAttachmentUrlsBySyllabusCourseId(@Param("courseId") Long courseId);

    /**
     * Delete all assignments under syllabuses of a given course.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Assignment a WHERE a.syllabus.id IN (SELECT s.id FROM Syllabus s WHERE s.course.id = :courseId)")
    void deleteAllBySyllabusCourseId(@Param("courseId") Long courseId);
}
