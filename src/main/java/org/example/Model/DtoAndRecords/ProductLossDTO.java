package org.example.Model.DtoAndRecords;

import java.math.BigDecimal;

public record ProductLossDTO(
        Long productId,
        String productName,
        BigDecimal totalLost
) {
}
