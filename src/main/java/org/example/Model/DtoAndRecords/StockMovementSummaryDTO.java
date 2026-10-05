package org.example.Model.DtoAndRecords;

import org.example.Events.StockMovementType;

public record StockMovementSummaryDTO(
        Long productId,
        String productName,
        StockMovementType movementType,
        Long quantity
) {
}