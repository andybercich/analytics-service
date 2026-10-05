package org.example.Repository;

import org.example.Model.GeneratedReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, Long> {
    List<GeneratedReport> findAllByOrderByGeneratedAtDesc();
}
