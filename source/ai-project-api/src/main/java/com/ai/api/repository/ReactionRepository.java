package com.ai.api.repository;

import com.ai.api.model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReactionRepository extends JpaRepository<Reaction, Long>, JpaSpecificationExecutor<Reaction> {
    Reaction findByCourseIdAndStudentIdAndStatus(Long courseId, Long studentId, Integer status);

    void deleteAllByCourseId(Long courseId);

    void deleteAllByStudentId(Long studentId);
}
