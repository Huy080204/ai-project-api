package com.ai.api.repository;

import com.ai.api.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long>, JpaSpecificationExecutor<Submission> {

    @Query("SELECT s.fileUrl FROM Submission s WHERE s.assignment.id = :assignmentId AND s.fileUrl IS NOT NULL AND s.fileUrl != ''")
    List<String> findFileUrlsByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Query("SELECT s.fileUrl FROM Submission s WHERE s.student.id = :studentId AND s.fileUrl IS NOT NULL AND s.fileUrl != ''")
    List<String> findFileUrlsByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT s.fileUrl FROM Submission s WHERE s.assignment.syllabus.id = :syllabusId AND s.fileUrl IS NOT NULL AND s.fileUrl != ''")
    List<String> findFileUrlsBySyllabusId(@Param("syllabusId") Long syllabusId);

    @Query("SELECT s.fileUrl FROM Submission s WHERE s.assignment.syllabus.course.id = :courseId AND s.fileUrl IS NOT NULL AND s.fileUrl != ''")
    List<String> findFileUrlsBySyllabusCourseId(@Param("courseId") Long courseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Submission s WHERE s.assignment.id = :assignmentId")
    void deleteAllByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Submission s WHERE s.student.id = :studentId")
    void deleteAllByStudentId(@Param("studentId") Long studentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Submission s WHERE s.assignment.id IN (SELECT a.id FROM Assignment a WHERE a.syllabus.id = :syllabusId)")
    void deleteAllBySyllabusId(@Param("syllabusId") Long syllabusId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Submission s WHERE s.assignment.id IN (SELECT a.id FROM Assignment a WHERE a.syllabus.course.id = :courseId)")
    void deleteAllBySyllabusCourseId(@Param("courseId") Long courseId);
}
