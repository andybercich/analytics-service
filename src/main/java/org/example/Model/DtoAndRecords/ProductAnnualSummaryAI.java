package org.example.Model.DtoAndRecords;

import java.math.BigDecimal;

public record ProductAnnualSummaryAI(
        Long productId,
        String productName,
        Integer appearancesAsTopSeller,
        Integer appearancesAsTopProfit,
        Integer appearancesAsTopLoss,
        Long totalQuantitySold,
        BigDecimal totalRevenue,
        BigDecimal totalProfit,
        BigDecimal totalLost
) {}