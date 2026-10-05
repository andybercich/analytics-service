package org.example.Model.DtoAndRecords;

import java.math.BigDecimal;

public record TopProductDTO(
        Long productId,
        String productName,
        Long totalQuantity,
        BigDecimal totalRevenue
) {}
