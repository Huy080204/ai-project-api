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

    @Modifying
    @Transactional
    @Query("DELETE FROM Submission s WHERE s.assignment.id = :assignmentId")
    void deleteAllByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Submission s WHERE s.student.id = :studentId")
    void deleteAllByStudentId(@Param("studentId") Long studentId);
}
