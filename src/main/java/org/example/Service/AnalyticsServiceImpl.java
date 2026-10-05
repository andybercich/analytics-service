package org.example.Service;


import lombok.RequiredArgsConstructor;
import org.example.Events.StockMovementType;
import org.example.Exception.ReportGenerationException;
import org.example.Model.DtoAndRecords.*;
import org.example.Model.Enum.AnalyticsProductType;
import org.example.Model.HourlyOrder;
import org.example.Model.MonthlyAnalytics;
import org.example.Model.MonthlyProductAnalytics;
import org.example.Repository.MonthlyAnalyticsRepository;
import org.example.Repository.MonthlyProductAnalyticsRepository;
import org.example.Repository.OrderAnalyticsRepository;
import org.example.Repository.ProductAnalyticsRepository;
import org.example.Service.Interfaces.AnalyticsInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsInterface {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

    private final OrderAnalyticsRepository orderAnalyticsRepository;
    private final ProductAnalyticsRepository productAnalyticsRepository;
    private final MonthlyProductAnalyticsRepository monthlyProductAnalyticsRepository;
    private final MonthlyAnalyticsRepository monthlyAnalyticsRepository;

    public AnnualAIData generateAnnualAIData(int year) {

        log.info("Starting annual AI analytics generation. year={}", year);

        if (year <= 0) {
            log.warn("Invalid year received for annual AI analytics. year={}", year);
            throw new IllegalArgumentException("El año debe ser un valor positivo.");
        }

        try {
            List<MonthlyAnalytics> monthlyAnalytics =
                    monthlyAnalyticsRepository.findByYearOrderByMonthAsc(year);

            if (monthlyAnalytics == null || monthlyAnalytics.isEmpty()) {
                log.warn("No monthly analytics data found. year={}", year);
                throw new IllegalStateException(
                        "No se encontraron datos de análisis mensual para el año: " + year
                );
            }

            List<MonthlyProductAnalytics> monthlyProducts =
                    monthlyProductAnalyticsRepository.findByYearOrderByMonthAscRankingAsc(year);

            if (monthlyProducts == null || monthlyProducts.isEmpty()) {
                log.warn("No monthly product analytics data found. year={}", year);
                throw new IllegalStateException(
                        "No se encontraron datos de productos mensuales para el año: " + year
                );
            }

            List<MonthlySummaryAI> monthlySummary = monthlyAnalytics.stream()
                    .map(this::buildMonthlySummaryAI)
                    .sorted(Comparator.comparing(MonthlySummaryAI::month))
                    .toList();

            List<ProductAnnualSummaryAI> productAnnualSummary =
                    buildProductAnnualSummary(monthlyProducts);

            AnnualAIData result = new AnnualAIData(
                    year,
                    monthlySummary,
                    productAnnualSummary
            );

            log.info(
                    "Annual AI analytics generated successfully. year={}, months={}, products={}",
                    year,
                    monthlySummary.size(),
                    productAnnualSummary.size()
            );

            return result;

        } catch (Exception e) {

            log.error(
                    "Error generating annual AI analytics. year={}",
                    year,
                    e
            );

            throw new ReportGenerationException(
                    "Error al generar los datos anuales de AI para el año: " + year,
                    e
            );
        }
    }

    private List<ProductAnnualSummaryAI> buildProductAnnualSummary(
            List<MonthlyProductAnalytics> products) {

        log.debug(
                "Building annual product summary. records={}",
                products.size()
        );
        try {

            Map<Long, List<MonthlyProductAnalytics>> groupedProducts = products.stream()
                            .collect(Collectors.groupingBy(MonthlyProductAnalytics::getProductId));

            return groupedProducts.entrySet()
                    .stream()
                    .map(entry -> {

                                Long productId = entry.getKey();
                                List<MonthlyProductAnalytics> monthlyData = entry.getValue();

                                String productName = monthlyData.get(0).getProductName();

                                long appearancesAsTopSeller = monthlyData.stream()
                                        .filter(m -> m.getType() == AnalyticsProductType.TOP_SELLER)
                                        .count();

                                long appearancesAsTopProfit = monthlyData.stream()
                                        .filter(m -> m.getType() == AnalyticsProductType.TOP_PROFIT)
                                        .count();

                                long appearancesAsTopLoss = monthlyData.stream()
                                        .filter(m -> m.getType() == AnalyticsProductType.TOP_LOSS)
                                        .count();

                                long totalQuantitySold = monthlyData.stream()
                                        .filter(m -> m.getQuantitySold() != null)
                                        .mapToLong(MonthlyProductAnalytics::getQuantitySold)
                                        .sum();

                                BigDecimal totalRevenue = monthlyData.stream()
                                        .filter(m -> m.getRevenue() != null)
                                        .map(MonthlyProductAnalytics::getRevenue)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                BigDecimal totalProfit = monthlyData.stream()
                                        .filter(m -> m.getProfit() != null)
                                        .map(MonthlyProductAnalytics::getProfit)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                BigDecimal totalLost = monthlyData.stream()
                                        .filter(m -> m.getQuantityLost() != null)
                                        .map(MonthlyProductAnalytics::getQuantityLost)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                return new ProductAnnualSummaryAI(
                                        productId,
                                        productName,
                                        (int) appearancesAsTopSeller,
                                        (int) appearancesAsTopProfit,
                                        (int) appearancesAsTopLoss,
                                        totalQuantitySold,
                                        totalRevenue,
                                        totalProfit,
                                        totalLost
                                );
                            })
                    .toList();
                } catch (Exception e) {
                    log.error("Error processing product annual summary", e);
                    throw new ReportGenerationException("Error processing product annual summary", e);
                }
    }

    private MonthlySummaryAI buildMonthlySummaryAI(MonthlyAnalytics analytics) {

        return new MonthlySummaryAI(
                analytics.getYear(),
                analytics.getMonth(),
                analytics.getTotalRevenue(),
                analytics.getTotalProfit(),
                analytics.getProfitMargin(),
                analytics.getTotalOrders(),
                analytics.getTotalProductsSold(),
                analytics.getDistinctProducts(),
                analytics.getAverageOrderValue(),
                analytics.getBestSalesDay(),
                analytics.getBestSalesDayOrders(),
                analytics.getBestRevenueDay(),
                analytics.getBestRevenueDayAmount(),
                analytics.getTotalUnitsLost()
        );
    }

    @Override
    public ReportDataDTO generateReportData(
            LocalDate start,
            LocalDate end) {

        log.info(
                "Starting report data generation. start={}, end={}",
                start,
                end
        );

        try {

            LocalDateTime startDateTime = start.atStartOfDay();
            LocalDateTime endDateTime = end.atTime(23, 59, 59);

            BigDecimal totalRevenue =
                    defaultBigDecimal(
                            orderAnalyticsRepository.getTotalRevenue(
                                    startDateTime,
                                    endDateTime
                            )
                    );

            BigDecimal totalProfit =
                    defaultBigDecimal(
                            orderAnalyticsRepository.getTotalProfit(
                                    startDateTime,
                                    endDateTime
                            )
                    );

            Long totalOrders =
                    defaultLong(
                            orderAnalyticsRepository.countOrders(
                                    startDateTime,
                                    endDateTime
                            )
                    );

            Long totalProductsSold =
                    defaultLong(
                            orderAnalyticsRepository.totalProductsSold(
                                    startDateTime,
                                    endDateTime
                            )
                    );

            Long distinctProducts =
                    defaultLong(
                            orderAnalyticsRepository.countDistinctProducts(
                                    startDateTime,
                                    endDateTime
                            )
                    );

            BigDecimal profitMargin =
                    calculateProfitMargin(
                            totalRevenue,
                            totalProfit
                    );

            BigDecimal averageOrderValue =
                    calculateAverageOrderValue(
                            totalRevenue,
                            totalOrders
                    );

            List<TopProductDTO> topProducts =
                    getTopProducts(
                            orderAnalyticsRepository.findTopProducts(
                                    startDateTime,
                                    endDateTime
                            )
                    );

            List<WeeklyRevenueDTO> revenueByWeek =
                    getWeeklyRevenue(
                            orderAnalyticsRepository.findRevenueByWeek(
                                    startDateTime,
                                    endDateTime
                            )
                    );

            List<WeeklyProfitDTO> profitByWeek =
                    getWeeklyProfit(
                            orderAnalyticsRepository.findProfitByWeek(
                                    startDateTime,
                                    endDateTime
                            )
                    );

            List<DailyOrdersDTO> ordersByDay =
                    orderAnalyticsRepository.findOrdersByDay(
                                    startDateTime,
                                    endDateTime
                            )
                            .stream()
                            .map(result -> new DailyOrdersDTO(
                                    extractDate(result, 0),
                                    extractLong(result, 1)
                            ))
                            .toList();

            List<HourlyOrder> ordersByHour =
                    orderAnalyticsRepository.findOrdersByHour(
                                    startDateTime,
                                    endDateTime
                            )
                            .stream()
                            .map(result -> new HourlyOrder(
                                    extractInteger(result, 0),
                                    extractLong(result, 1)
                            ))
                            .toList();

            List<Object[]> bestSalesDayResults =
                    orderAnalyticsRepository.findBestSalesDay(
                            startDateTime,
                            endDateTime
                    );

            Object[] bestSalesDayResult =
                    bestSalesDayResults.isEmpty()
                            ? null
                            : bestSalesDayResults.get(0);

            LocalDate bestSalesDay =
                    extractDate(bestSalesDayResult, 0);

            Long bestSalesDayOrders =
                    extractLong(bestSalesDayResult, 1);

            List<Object[]> bestRevenueDayResults =
                    orderAnalyticsRepository.findBestRevenueDay(
                            startDateTime,
                            endDateTime
                    );

            Object[] bestRevenueDayResult =
                    bestRevenueDayResults.isEmpty()
                            ? null
                            : bestRevenueDayResults.get(0);

            LocalDate bestRevenueDay =
                    extractDate(bestRevenueDayResult, 0);

            BigDecimal bestRevenueDayAmount =
                    extractBigDecimal(
                            bestRevenueDayResult,
                            1
                    );

            List<StockMovementSummaryDTO> stockMovements =
                    getStockMovementsSummary(
                            productAnalyticsRepository.movementSummaryByProduct(
                                    startDateTime,
                                    endDateTime
                            )
                    );

            List<ProductLossDTO> topLossProducts =
                    getTopLossProducts(
                            orderAnalyticsRepository.findTopLossProducts(
                                    startDateTime,
                                    endDateTime
                            )
                    );

            List<ProductProfitDTO> topProfitProducts =
                    getTopProfitProducts(
                            orderAnalyticsRepository.findTopProfitProducts(
                                    startDateTime,
                                    endDateTime
                            )
                    );

            ReportDataDTO result = new ReportDataDTO(
                    totalRevenue,
                    totalProfit,
                    profitMargin,
                    averageOrderValue,
                    totalOrders,
                    totalProductsSold,
                    distinctProducts,
                    bestSalesDay,
                    bestSalesDayOrders,
                    bestRevenueDay,
                    bestRevenueDayAmount,
                    topProducts,
                    revenueByWeek,
                    profitByWeek,
                    stockMovements,
                    topLossProducts,
                    topProfitProducts,
                    ordersByDay,
                    ordersByHour
            );

            log.info(
                    "Report data generated successfully. start={}, end={}, orders={}, productsSold={}, revenue={}, profit={}",
                    start,
                    end,
                    totalOrders,
                    totalProductsSold,
                    totalRevenue,
                    totalProfit
            );

            return result;

        } catch (Exception e) {

            log.error(
                    "Error generating report data. start={}, end={}",
                    start,
                    end,
                    e
            );

            throw new ReportGenerationException(
                    "Error al generar los datos anuales de AI para el año: " + start, e
            );
        }
    }

    private BigDecimal calculateProfitMargin(
            BigDecimal revenue,
            BigDecimal profit
    ) {

        if (revenue == null ||
                revenue.compareTo(BigDecimal.ZERO) == 0) {

            return BigDecimal.ZERO;
        }

        return profit
                .divide(
                        revenue,
                        4,
                        RoundingMode.HALF_UP
                )
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAverageOrderValue(
            BigDecimal revenue,
            Long orders
    ) {

        if (revenue == null ||
                orders == null ||
                orders == 0) {

            return BigDecimal.ZERO;
        }

        return revenue.divide(
                BigDecimal.valueOf(orders),
                2,
                RoundingMode.HALF_UP
        );
    }

    private BigDecimal defaultBigDecimal(
            BigDecimal value
    ) {

        return value != null
                ? value
                : BigDecimal.ZERO;
    }

    private Long defaultLong(Long value) {

        return value != null
                ? value
                : 0L;
    }

    private LocalDate extractDate(
            Object[] row,
            int index
    ) {

        if (row == null ||
                row.length <= index ||
                row[index] == null) {

            return null;
        }

        Object value = row[index];

        if (value instanceof LocalDate date) {
            return date;
        }

        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }

        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp
                    .toLocalDateTime()
                    .toLocalDate();
        }

        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toLocalDate();
        }

        throw new IllegalStateException(
                "Tipo inesperado para fecha: "
                        + value.getClass()
        );
    }

    private Long extractLong(
            Object[] row,
            int index
    ) {

        if (row == null ||
                row.length <= index ||
                row[index] == null) {

            return 0L;
        }

        return ((Number) row[index]).longValue();
    }


    private BigDecimal extractBigDecimal(
            Object[] row,
            int index
    ) {

        if (row == null ||
                row.length <= index ||
                row[index] == null) {

            return BigDecimal.ZERO;
        }

        Object value = row[index];

        if (value instanceof BigDecimal decimal) {
            return decimal;
        }

        if (value instanceof Number number) {
            return BigDecimal.valueOf(
                    number.doubleValue()
            );
        }

        throw new IllegalStateException(
                "Tipo inesperado para BigDecimal: "
                        + value.getClass()
        );
    }



    // ==============================
    // PRODUCTS
    // ==============================

    private List<TopProductDTO> getTopProducts(
            List<Object[]> results
    ) {

        if (results == null ||
                results.isEmpty()) {

            return Collections.emptyList();
        }

        return results.stream()
                .map(row -> new TopProductDTO(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        null
                ))
                .toList();
    }

    // ==============================
    // GAINS PER WEEK
    // ==============================

    private List<WeeklyRevenueDTO> getWeeklyRevenue(
            List<Object[]> results
    ) {

        if (results == null ||
                results.isEmpty()) {

            return Collections.emptyList();
        }

        return results.stream()
                .map(row -> {

                    LocalDate weekStart =
                            extractDate(row, 0);

                    BigDecimal revenue =
                            extractBigDecimal(row, 1);

                    return new WeeklyRevenueDTO(
                            weekStart,
                            revenue
                    );
                })
                .sorted(
                        Comparator.comparing(
                                WeeklyRevenueDTO::weekStart
                        )
                )
                .toList();
    }

    // ==============================
    // GAINS
    // ==============================

    private List<WeeklyProfitDTO> getWeeklyProfit(
            List<Object[]> results
    ) {

        if (results == null ||
                results.isEmpty()) {

            return Collections.emptyList();
        }

        return results.stream()
                .map(row -> {

                    LocalDate weekStart =
                            extractDate(row, 0);

                    BigDecimal profit =
                            extractBigDecimal(row, 1);

                    return new WeeklyProfitDTO(
                            weekStart,
                            profit
                    );
                })
                .sorted(
                        Comparator.comparing(
                                WeeklyProfitDTO::weekStart
                        )
                )
                .toList();
    }

    // ==============================
    // STOCK
    // ==============================

    private List<StockMovementSummaryDTO> getStockMovementsSummary(
            List<Object[]> results
    ) {

        if (results == null ||
                results.isEmpty()) {

            return Collections.emptyList();
        }

        return results.stream()
                .map(row ->
                        new StockMovementSummaryDTO(
                                ((Number) row[0]).longValue(),
                                (String) row[1],
                                (StockMovementType) row[2],
                                ((Number) row[3]).longValue()
                        )
                )
                .toList();
    }

    // ==============================
    // LOSSES
    // ==============================

    private List<ProductLossDTO> getTopLossProducts(
            List<Object[]> results
    ) {

        if (results == null ||
                results.isEmpty()) {

            return Collections.emptyList();
        }

        return results.stream()
                .map(row ->
                        new ProductLossDTO(
                                ((Number) row[0]).longValue(),
                                (String) row[1],
                                (BigDecimal) row[2]
                        )
                )
                .toList();
    }

    // ==============================
    // GAINS
    // ==============================

    private List<ProductProfitDTO> getTopProfitProducts(
            List<Object[]> results
    ) {

        if (results == null ||
                results.isEmpty()) {

            return Collections.emptyList();
        }

        return results.stream()
                .map(row ->
                        new ProductProfitDTO(
                                ((Number) row[0]).longValue(),
                                (String) row[1],
                                (BigDecimal) row[2]
                        )
                )
                .toList();
    }



    private Integer extractInteger(Object[] result, int index) {
        if (result[index] == null) {
            return 0;
        }

        return ((Number) result[index]).intValue();
    }
}