package org.example.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "monthly_analytics",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"year", "month"}
                )
        }
)
@Getter
@Setter
public class MonthlyAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer year;

    private Integer month;

    private BigDecimal totalRevenue;

    private BigDecimal totalProfit;

    private BigDecimal profitMargin;

    private Long totalOrders;

    private Long totalProductsSold;

    private Long distinctProducts;

    private BigDecimal averageOrderValue;

    private LocalDate bestSalesDay;

    private Long bestSalesDayOrders;

    private LocalDate bestRevenueDay;

    private BigDecimal bestRevenueDayAmount;

    private Long totalUnitsLost;

    private LocalDateTime generatedAt;

    @ElementCollection
    @CollectionTable(
            name = "monthly_orders_by_hour",
            joinColumns = @JoinColumn(name = "monthly_analytics_id")
    )
    private List<HourlyOrder> ordersByHour = new ArrayList<>();

}
