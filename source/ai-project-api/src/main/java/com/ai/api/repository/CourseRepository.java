package com.ai.api.repository;

import com.ai.api.dto.report.CourseReportDto;
import com.ai.api.model.Course;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Course c SET c.totalTimeline = COALESCE(c.totalTimeline, 0) + :delta WHERE c.id = :courseId")
    void updateTotalTimelineByDelta(@Param("courseId") Long courseId, @Param("delta") Integer delta);

    // cs.state = 1 -> AIConstant.CLASSROOM_STUDENT_STATE_ACCEPT
    // (JPQL string literals can't reference Java constants directly)
    @Query("SELECT new com.ai.api.dto.report.CourseReportDto(c.id, c.name, c.avatar, COUNT(DISTINCT cs.student.id)) " +
            "FROM Course c JOIN Classroom cl ON cl.course = c JOIN ClassroomStudent cs ON cs.classroom = cl " +
            "WHERE c.status = :status AND cs.state = 1 " +
            "GROUP BY c.id, c.name, c.avatar ORDER BY COUNT(DISTINCT cs.student.id) DESC")
    List<CourseReportDto> findTopCourseReport(@Param("status") Integer status, Pageable pageable);
}
