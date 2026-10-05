package org.example.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.Model.Enum.AnalyticsProductType;

import java.math.BigDecimal;

@Entity
@Table(name = "monthly_product_analytics",
        uniqueConstraints = {@UniqueConstraint(name = "uk_monthly_product_type",
                        columnNames = {"year", "month", "product_id", "type"})}
)
@Getter
@Setter
public class MonthlyProductAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer year;

    private Integer month;

    private Long productId;

    private String productName;

    private Long quantitySold;

    private BigDecimal quantityLost;

    private BigDecimal revenue;

    private BigDecimal profit;

    private Integer ranking;

    @Enumerated(EnumType.STRING)
    private AnalyticsProductType type;
}
