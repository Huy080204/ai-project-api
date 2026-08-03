package com.ai.api.repository;

import com.ai.api.model.ClassroomStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ClassroomStudentRepository extends JpaRepository<ClassroomStudent, Long>, JpaSpecificationExecutor<ClassroomStudent> {
    boolean existsByClassroomIdAndStudentId(Long classroomId, Long studentId);

    boolean existsByClassroomIdAndStudentAccountEmail(Long classroomId, String email);

    boolean existsByClassroomIdAndStudentAccountPhone(Long classroomId, String phone);

    @Modifying
    @Transactional
    @Query("DELETE FROM ClassroomStudent cs WHERE cs.classroom.id = :classroomId")
    void deleteAllByClassroomId(@Param("classroomId") Long classroomId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ClassroomStudent cs WHERE cs.classroom.id IN (SELECT c.id FROM Classroom c WHERE c.course.id = :courseId)")
    void deleteAllByClassroomCourseId(@Param("courseId") Long courseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ClassroomStudent cs WHERE cs.student.id = :studentId")
    void deleteAllByStudentId(@Param("studentId") Long studentId);
}
