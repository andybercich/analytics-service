package org.example.Repository;

import org.example.Model.MonthlyAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyAnalyticsRepository extends JpaRepository<MonthlyAnalytics, Long> {

    List<MonthlyAnalytics> findByYearOrderByMonthAsc(Integer year);

    Optional<MonthlyAnalytics> findByYearAndMonth(
            Integer year,
            Integer month
    );
}
