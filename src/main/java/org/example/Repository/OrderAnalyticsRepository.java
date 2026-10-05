package org.example.Repository;

import org.example.Model.OrderAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderAnalyticsRepository extends JpaRepository<OrderAnalytics, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    DELETE FROM OrderAnalytics o
    WHERE o.orderId = :orderId
""")
    void deleteByOrderId(@Param("orderId") Long orderId);

    @Query("""
        SELECT SUM(o.total)
        FROM OrderAnalytics o
        WHERE o.createdAt BETWEEN :start AND :end
        """)
    BigDecimal getTotalRevenue(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
        SELECT SUM(o.profit)
        FROM OrderAnalytics o
        WHERE o.createdAt BETWEEN :start AND :end
        """)
    BigDecimal getTotalProfit(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
        SELECT COUNT(DISTINCT o.orderId)
        FROM OrderAnalytics o
        WHERE o.createdAt BETWEEN :start AND :end
        """)
    Long countOrders(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
        SELECT SUM(o.quantity)
        FROM OrderAnalytics o
        WHERE o.createdAt BETWEEN :start AND :end
        """)
    Long totalProductsSold(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
        SELECT COUNT(DISTINCT o.productId)
        FROM OrderAnalytics o
        WHERE o.createdAt BETWEEN :start AND :end
        """)
    Long countDistinctProducts(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query(value = """
        SELECT
            product_id,
            product_name,
            SUM(quantity) AS total_sold
        FROM order_analytics
        WHERE created_at BETWEEN :start AND :end
        GROUP BY product_id, product_name
        ORDER BY total_sold DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Object[]> findTopProducts(
            LocalDateTime start,
            LocalDateTime end
    );

    //Ingresos agrupados por semana.La semana comienza el lunes.

    @Query(value = """
        SELECT
            DATE_SUB(
                DATE(created_at),
                INTERVAL WEEKDAY(created_at) DAY
            ) AS week_start,
            COALESCE(SUM(total), 0) AS revenue
        FROM order_analytics
        WHERE created_at BETWEEN :start AND :end
        GROUP BY
            DATE_SUB(
                DATE(created_at),
                INTERVAL WEEKDAY(created_at) DAY
            )
        ORDER BY week_start ASC
        """, nativeQuery = true)
    List<Object[]> findRevenueByWeek(
            LocalDateTime start,
            LocalDateTime end
    );

    /**
     * Ganancias agrupadas por semana.
     * La semana comienza el lunes.
     *
     * Retorna:
     * [week_start, profit]
     */
    @Query(value = """
        SELECT
            DATE_SUB(
                DATE(created_at),
                INTERVAL WEEKDAY(created_at) DAY
            ) AS week_start,
            COALESCE(SUM(profit), 0) AS profit
        FROM order_analytics
        WHERE created_at BETWEEN :start AND :end
        GROUP BY
            DATE_SUB(
                DATE(created_at),
                INTERVAL WEEKDAY(created_at) DAY
            )
        ORDER BY week_start ASC
        """, nativeQuery = true)
    List<Object[]> findProfitByWeek(
            LocalDateTime start,
            LocalDateTime end
    );

    /**
     * Top 10 productos con mayores pérdidas.
     *
     * Pérdida =
     * (unit_cost - unit_price) * quantity
     */
    @Query(value = """
        SELECT
            product_id,
            product_name,
            COALESCE(
                SUM((unit_cost - unit_price) * quantity),
                0
            ) AS total_loss
        FROM order_analytics
        WHERE created_at BETWEEN :start AND :end
          AND unit_cost > unit_price
        GROUP BY product_id, product_name
        HAVING SUM((unit_cost - unit_price) * quantity) > 0
        ORDER BY total_loss DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Object[]> findTopLossProducts(
            LocalDateTime start,
            LocalDateTime end
    );

    /**
     * Top 10 productos con mayores ganancias.
     *
     * Ganancia =
     * (unit_price - unit_cost) * quantity
     */
    @Query(value = """
        SELECT
            product_id,
            product_name,
            COALESCE(
                SUM((unit_price - unit_cost) * quantity),
                0
            ) AS total_profit
        FROM order_analytics
        WHERE created_at BETWEEN :start AND :end
          AND unit_price > unit_cost
        GROUP BY product_id, product_name
        HAVING SUM((unit_price - unit_cost) * quantity) > 0
        ORDER BY total_profit DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Object[]> findTopProfitProducts(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
    SELECT
        DATE(o.createdAt),
        COUNT(o.id)
    FROM OrderAnalytics o
    WHERE o.createdAt BETWEEN :start AND :end
    GROUP BY DATE(o.createdAt)
    ORDER BY COUNT(o.id) DESC
    LIMIT 1
    """)
    List<Object[]> findBestSalesDay(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
    SELECT
        DATE(o.createdAt),
        SUM(o.total)
    FROM OrderAnalytics o
    WHERE o.createdAt BETWEEN :start AND :end
    GROUP BY DATE(o.createdAt)
    ORDER BY SUM(o.total) DESC
    LIMIT 1
    """)
    List<Object[]> findBestRevenueDay(
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
    SELECT FUNCTION('DATE', o.createdAt), COUNT(o.id)
    FROM OrderAnalytics o
    WHERE o.createdAt >= :startDateTime
      AND o.createdAt < :endDateTime
    GROUP BY FUNCTION('DATE', o.createdAt)
    ORDER BY FUNCTION('DATE', o.createdAt)
""")
    List<Object[]> findOrdersByDay(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    @Query("""
    SELECT FUNCTION('HOUR', o.createdAt), COUNT(o.id)
    FROM OrderAnalytics o
    WHERE o.createdAt >= :startDateTime
      AND o.createdAt < :endDateTime
    GROUP BY FUNCTION('HOUR', o.createdAt)
    ORDER BY FUNCTION('HOUR', o.createdAt)
""")
    List<Object[]> findOrdersByHour(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}