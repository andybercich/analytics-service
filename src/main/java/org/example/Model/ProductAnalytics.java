package org.example.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.Events.StockMovementType;

import java.time.LocalDateTime;


@Entity @Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private String productName;

    private String category;

    @Enumerated(EnumType.STRING)
    private StockMovementType movementType;

    private Integer quantityChanged;

    private Integer stockBefore;

    private Integer stockAfter;

    private LocalDateTime timestamp;

}
