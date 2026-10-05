package org.example.Model.DtoAndRecords;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.Model.HourlyOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReportDataDTO(

        BigDecimal totalRevenue,

        BigDecimal totalProfit,

        BigDecimal profitMargin,

        BigDecimal averageOrderValue,

        Long totalOrders,

        Long totalProductsSold,

        Long distinctProducts,

        LocalDate bestSalesDay,

        Long bestSalesDayOrders,

        LocalDate bestRevenueDay,

        BigDecimal bestRevenueDayAmount,

        List<TopProductDTO> topProducts,

        List<WeeklyRevenueDTO> revenueByWeek,

        List<WeeklyProfitDTO> profitByWeek,

        List<StockMovementSummaryDTO> stockMovements,

        List<ProductLossDTO> topLossProducts,

        List<ProductProfitDTO> topProfitProducts,

        List<DailyOrdersDTO> dailyOrders,
        List<HourlyOrder> ordersByHour

) {}
