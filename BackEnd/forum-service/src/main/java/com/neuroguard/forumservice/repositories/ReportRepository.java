package com.neuroguard.forumservice.repositories;

import com.neuroguard.forumservice.entities.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {}