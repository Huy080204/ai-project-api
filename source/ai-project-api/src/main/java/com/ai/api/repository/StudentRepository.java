package com.ai.api.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ai.api.dto.report.StudentReportDto;
import com.ai.api.model.Student;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
    Optional<Student> findFirstByAccountPhoneOrAccountEmail(String phone, String email);

    Optional<Student> findFirstByAccountPhone(String phone);

    Optional<Student> findByIdAndStatus(Long id, Integer status);

    // cs.state = 1 -> AIConstant.CLASSROOM_STUDENT_STATE_ACCEPT
    // (JPQL string literals can't reference Java constants directly)
    @Query("SELECT new com.ai.api.dto.report.StudentReportDto(s.id, a.fullName, a.email, a.phone, a.avatarPath, COUNT(cs.id)) " +
            "FROM Student s JOIN s.account a JOIN ClassroomStudent cs ON cs.student = s " +
            "WHERE s.status = :status AND cs.state = 1 " +
            "GROUP BY s.id, a.fullName, a.email, a.phone, a.avatarPath ORDER BY COUNT(cs.id) DESC")
    List<StudentReportDto> findTopStudentReport(@Param("status") Integer status, Pageable pageable);
}
