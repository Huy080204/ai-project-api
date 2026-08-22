package com.ai.api.repository;

import com.ai.api.model.NotificationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationGroupRepository extends JpaRepository<NotificationGroup, Long>, JpaSpecificationExecutor<NotificationGroup> {
    boolean existsByNameAndClassroomId(String name, Long classroomId);

    boolean existsByNameAndClassroomIdAndIdNot(String name, Long classroomId, Long id);

    @Modifying
    @Transactional
    @Query("DELETE FROM NotificationGroup n WHERE n.classroom.id = :classroomId")
    void deleteAllByClassroomId(@Param("classroomId") Long classroomId);

    @Modifying
    @Transactional
    @Query("DELETE FROM NotificationGroup n WHERE n.classroom.id IN (SELECT c.id FROM Classroom c WHERE c.course.id = :courseId)")
    void deleteAllByClassroomCourseId(@Param("courseId") Long courseId);

    @Query("SELECT n.avatar FROM NotificationGroup n WHERE n.classroom.id = :classroomId")
    List<String> findAvatarsByClassroomId(@Param("classroomId") Long classroomId);

    @Query("SELECT n.avatar FROM NotificationGroup n WHERE n.classroom.course.id = :courseId")
    List<String> findAvatarsByClassroomCourseId(@Param("courseId") Long courseId);
}
