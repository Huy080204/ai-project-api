package com.ai.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.ai.api.model.Mentor;

public interface MentorRepository extends JpaRepository<Mentor, Long>, JpaSpecificationExecutor<Mentor> {
}
