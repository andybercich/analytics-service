package org.example.Repository;

import org.example.Model.ProductAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductAnalyticsRepository
        extends JpaRepository<ProductAnalytics, Long> {

    List<ProductAnalytics> findByTimestampBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
    SELECT
        p.productId,
        p.productName,
        p.movementType,
        SUM(p.quantityChanged)
    FROM ProductAnalytics p
    WHERE p.timestamp BETWEEN :start AND :end
    GROUP BY
        p.productId,
        p.productName,
        p.movementType
    ORDER BY
        p.productName,
        p.movementType
    """)
    List<Object[]> movementSummaryByProduct(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
        SELECT COUNT(p)
        FROM ProductAnalytics p
        WHERE p.timestamp BETWEEN :start AND :end
        """)
    Long countMovements(
            LocalDateTime start,
            LocalDateTime end
    );
}