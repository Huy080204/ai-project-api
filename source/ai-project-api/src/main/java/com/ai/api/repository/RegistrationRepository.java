package com.ai.api.repository;

import com.ai.api.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RegistrationRepository extends JpaRepository<Registration, Long>, JpaSpecificationExecutor<Registration> {
    boolean existsByClassroomIdAndEmail(Long classroomId, String email);

    boolean existsByClassroomIdAndPhone(Long classroomId, String phone);

    @Modifying
    @Transactional
    @Query("DELETE FROM Registration r WHERE r.classroom.id = :classroomId")
    void deleteAllByClassroomId(@Param("classroomId") Long classroomId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Registration r WHERE r.classroom.id IN (SELECT c.id FROM Classroom c WHERE c.course.id = :courseId)")
    void deleteAllByClassroomCourseId(@Param("courseId") Long courseId);
}
