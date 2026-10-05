package org.example.Model.DtoAndRecords;

import java.math.BigDecimal;

public record AnnualSummaryAI(
        BigDecimal totalRevenue,
        BigDecimal totalProfit,
        Long totalOrders,
        Long totalProductsSold,
        BigDecimal averageOrderValue,
        BigDecimal profitMargin,
        String bestMonth,
        BigDecimal bestMonthRevenue,
        String worstMonth,
        BigDecimal worstMonthRevenue
) {}
