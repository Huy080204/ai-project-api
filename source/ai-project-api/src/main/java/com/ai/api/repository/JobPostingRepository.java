package com.ai.api.repository;

import com.ai.api.model.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long>, JpaSpecificationExecutor<JobPosting> {

    @Modifying
    @Transactional
    @Query("DELETE FROM JobPosting jp WHERE jp.company.id = :companyId")
    void deleteAllByCompanyId(@Param("companyId") Long companyId);
}
