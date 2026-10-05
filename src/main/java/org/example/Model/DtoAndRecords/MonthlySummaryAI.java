package org.example.Model.DtoAndRecords;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MonthlySummaryAI(
        Integer year,
        Integer month,
        BigDecimal totalRevenue,
        BigDecimal totalProfit,
        BigDecimal profitMargin,
        Long totalOrders,
        Long totalProductsSold,
        Long distinctProducts,
        BigDecimal averageOrderValue,
        LocalDate bestSalesDay,
        Long bestSalesDayOrders,
        LocalDate bestRevenueDay,
        BigDecimal bestRevenueDayAmount,
        Long totalUnitsLost
) {}