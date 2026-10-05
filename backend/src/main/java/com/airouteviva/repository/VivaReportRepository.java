package com.airouteviva.repository;

import com.airouteviva.entity.VivaReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VivaReportRepository extends JpaRepository<VivaReport, Long> {
    Optional<VivaReport> findByReportId(String reportId);
    Optional<VivaReport> findBySessionId(String sessionId);
}
