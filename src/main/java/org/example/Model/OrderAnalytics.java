package org.example.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.Events.ItemType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "order_analytics",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_order_product",
                        columnNames = {"order_id", "product_id"}
                )
        }
)
public class OrderAnalytics {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private ItemType itemType;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal unitCost;
    private BigDecimal profit;

    private BigDecimal total;

    private LocalDateTime createdAt;
}
