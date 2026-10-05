package org.example.Model.DtoAndRecords;

import java.math.BigDecimal;

public record ProductProfitDTO(
        Long productId,
        String productName,
        BigDecimal totalProfit
) {
}