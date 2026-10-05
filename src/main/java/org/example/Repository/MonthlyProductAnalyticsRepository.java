package org.example.Repository;

import org.example.Model.MonthlyProductAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyProductAnalyticsRepository extends JpaRepository<MonthlyProductAnalytics, Long> {
    void deleteByYearAndMonth(Integer year, Integer month);

    List<MonthlyProductAnalytics> findByYearOrderByMonthAscRankingAsc(Integer year);

}
