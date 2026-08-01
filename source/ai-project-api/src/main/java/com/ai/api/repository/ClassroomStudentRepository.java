package com.ai.api.repository;

import com.ai.api.model.ClassroomStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ClassroomStudentRepository extends JpaRepository<ClassroomStudent, Long>, JpaSpecificationExecutor<ClassroomStudent> {
    boolean existsByClassroom_IdAndStudent_Id(Long classroomId, Long studentId);

    boolean existsByClassroom_IdAndStudent_Account_Email(Long classroomId, String email);

    boolean existsByClassroom_IdAndStudent_Account_Phone(Long classroomId, String phone);

    @Modifying
    @Transactional
    @Query("DELETE FROM ClassroomStudent cs WHERE cs.classroom.id = :classroomId")
    void deleteAllByClassroomId(@Param("classroomId") Long classroomId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ClassroomStudent cs WHERE cs.classroom.course.id = :courseId")
    void deleteAllByClassroomCourseId(@Param("courseId") Long courseId);
}
