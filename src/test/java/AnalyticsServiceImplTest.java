import org.example.Model.DtoAndRecords.*;
import org.example.Repository.OrderAnalyticsRepository;
import org.example.Repository.ProductAnalyticsRepository;
import org.example.Service.AnalyticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private OrderAnalyticsRepository orderAnalyticsRepository;

    @Mock
    private ProductAnalyticsRepository productAnalyticsRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private LocalDate start;
    private LocalDate end;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    @BeforeEach
    void setUp() {

        start = LocalDate.of(2026, 8, 1);
        end = LocalDate.of(2026, 8, 31);

        startDateTime = start.atStartOfDay();
        endDateTime = end.atTime(23, 59, 59);
    }



    // 1. MÉTRICAS PRINCIPALES
    @Test
    void  generateReportData_shouldCalculateMainMetrics() {
        when(orderAnalyticsRepository.getTotalRevenue(startDateTime, endDateTime))
                .thenReturn(new BigDecimal("1000000"));
        when(orderAnalyticsRepository.getTotalProfit(startDateTime, endDateTime))
                .thenReturn(new BigDecimal("300000"));
        when(orderAnalyticsRepository.countOrders(startDateTime, endDateTime))
                .thenReturn(100L);
        when(orderAnalyticsRepository.totalProductsSold(startDateTime, endDateTime))
                .thenReturn(250L);
        when(orderAnalyticsRepository.countDistinctProducts(startDateTime, endDateTime))
                .thenReturn(20L);

        mockEmptyQueries();

        ReportDataDTO result = analyticsService.generateReportData(start, end);

        assertNotNull(result);
        assertEquals(new BigDecimal("1000000"), result.totalRevenue());
        assertEquals(new BigDecimal("300000"), result.totalProfit());
        assertEquals(100L, result.totalOrders());
        assertEquals(250L, result.totalProductsSold());
        assertEquals(20L, result.distinctProducts());

        verify(orderAnalyticsRepository).getTotalRevenue(startDateTime, endDateTime);
        verify(orderAnalyticsRepository).getTotalProfit(startDateTime, endDateTime);
        verify(orderAnalyticsRepository).countOrders(startDateTime, endDateTime);
        verify(orderAnalyticsRepository).totalProductsSold(startDateTime, endDateTime);
        verify(orderAnalyticsRepository).countDistinctProducts(startDateTime, endDateTime);
    }


    // 2. PROFIT MARGIN


    @Test
    void generateReportData_shouldCalculateProfitMargin() {

        when(orderAnalyticsRepository.getTotalRevenue(
                startDateTime,
                endDateTime
        )).thenReturn(new BigDecimal("1000000"));

        when(orderAnalyticsRepository.getTotalProfit(
                startDateTime,
                endDateTime
        )).thenReturn(new BigDecimal("250000"));

        when(orderAnalyticsRepository.countOrders(
                startDateTime,
                endDateTime
        )).thenReturn(100L);

        when(orderAnalyticsRepository.totalProductsSold(
                startDateTime,
                endDateTime
        )).thenReturn(200L);

        when(orderAnalyticsRepository.countDistinctProducts(
                startDateTime,
                endDateTime
        )).thenReturn(10L);

        mockEmptyQueries();

        ReportDataDTO result =
                analyticsService.generateReportData(start, end);

        assertNotNull(result.profitMargin());

        assertEquals(
                new BigDecimal("25.00"),
                result.profitMargin()
        );
    }


    // 3. AVERAGE ORDER VALUE

    @Test
    void generateReportData_shouldCalculateAverageOrderValue() {

        when(orderAnalyticsRepository.getTotalRevenue(
                startDateTime,
                endDateTime
        )).thenReturn(new BigDecimal("1000000"));

        when(orderAnalyticsRepository.getTotalProfit(
                startDateTime,
                endDateTime
        )).thenReturn(new BigDecimal("200000"));

        when(orderAnalyticsRepository.countOrders(
                startDateTime,
                endDateTime
        )).thenReturn(100L);

        when(orderAnalyticsRepository.totalProductsSold(
                startDateTime,
                endDateTime
        )).thenReturn(200L);

        when(orderAnalyticsRepository.countDistinctProducts(
                startDateTime,
                endDateTime
        )).thenReturn(10L);

        mockEmptyQueries();

        ReportDataDTO result =
                analyticsService.generateReportData(start, end);

        assertEquals(
                new BigDecimal("10000.00"),
                result.averageOrderValue()
        );
    }


    //  4 ORDERS BY DAY

    @Test
    void generateReportData_shouldReturnOrdersByDay() {

        Object[] day1 = {
                java.sql.Date.valueOf("2026-08-01"),
                20L
        };

        Object[] day2 = {
                java.sql.Date.valueOf("2026-08-02"),
                35L
        };

        when(orderAnalyticsRepository.getTotalRevenue(
                startDateTime, endDateTime
        )).thenReturn(new BigDecimal("800000"));

        when(orderAnalyticsRepository.getTotalProfit(
                startDateTime, endDateTime
        )).thenReturn(new BigDecimal("200000"));

        when(orderAnalyticsRepository.countOrders(
                startDateTime, endDateTime
        )).thenReturn(55L);

        when(orderAnalyticsRepository.totalProductsSold(
                startDateTime, endDateTime
        )).thenReturn(100L);

        when(orderAnalyticsRepository.countDistinctProducts(
                startDateTime, endDateTime
        )).thenReturn(5L);

        when(orderAnalyticsRepository.findOrdersByDay(
                startDateTime,
                endDateTime
        )).thenReturn(List.of(day1, day2));

        mockEmptyQueriesExceptOrdersByDay();

        ReportDataDTO result =
                analyticsService.generateReportData(start, end);

        assertNotNull(result.ordersByHour());

        assertEquals(2, result.dailyOrders().size());

        assertEquals(
                LocalDate.of(2026, 8, 1),
                result.dailyOrders().get(0).date()
        );

        assertEquals(
                20L,
                result.dailyOrders().get(0).orderCount()
        );
    }

    // 5 ORDERS BY HOUR


    @Test
    void generateReportData_shouldReturnOrdersByHour() {

        Object[] hour1 = {
                9,
                25L
        };

        Object[] hour2 = {
                14,
                40L
        };

        when(orderAnalyticsRepository.getTotalRevenue(
                startDateTime, endDateTime
        )).thenReturn(new BigDecimal("800000"));

        when(orderAnalyticsRepository.getTotalProfit(
                startDateTime, endDateTime
        )).thenReturn(new BigDecimal("200000"));

        when(orderAnalyticsRepository.countOrders(
                startDateTime, endDateTime
        )).thenReturn(65L);

        when(orderAnalyticsRepository.totalProductsSold(
                startDateTime, endDateTime
        )).thenReturn(100L);

        when(orderAnalyticsRepository.countDistinctProducts(
                startDateTime, endDateTime
        )).thenReturn(5L);

        when(orderAnalyticsRepository.findOrdersByHour(
                startDateTime,
                endDateTime
        )).thenReturn(List.of(hour1, hour2));

        mockEmptyQueriesExceptOrdersByHour();

        ReportDataDTO result =
                analyticsService.generateReportData(start, end);

        assertNotNull(result.ordersByHour());

        assertEquals(2, result.ordersByHour().size());

        assertEquals(
                9,
                result.ordersByHour().get(0).getHour()
        );

        assertEquals(
                25L,
                result.ordersByHour().get(0).getOrderCount()
        );

        assertEquals(
                14,
                result.ordersByHour().get(1).getHour()
        );

        assertEquals(
                40L,
                result.ordersByHour().get(1).getOrderCount()
        );
    }


    // 6 BEST SALES DAY

    @Test
    void generateReportData_shouldReturnBestSalesDay() {

        Object[] bestDay = {
                java.sql.Date.valueOf("2026-08-19"),
                75L
        };

        when(orderAnalyticsRepository.getTotalRevenue(
                startDateTime, endDateTime
        )).thenReturn(new BigDecimal("1000000"));

        when(orderAnalyticsRepository.getTotalProfit(
                startDateTime, endDateTime
        )).thenReturn(new BigDecimal("300000"));

        when(orderAnalyticsRepository.countOrders(
                startDateTime, endDateTime
        )).thenReturn(200L);

        when(orderAnalyticsRepository.totalProductsSold(
                startDateTime, endDateTime
        )).thenReturn(300L);

        when(orderAnalyticsRepository.countDistinctProducts(
                startDateTime, endDateTime
        )).thenReturn(10L);

        when(orderAnalyticsRepository.findBestSalesDay(
                startDateTime,
                endDateTime
        )).thenReturn(Collections.singletonList(bestDay));

        mockEmptyQueriesExceptBestSalesDay();

        ReportDataDTO result =
                analyticsService.generateReportData(start, end);

        assertNotNull(result.bestSalesDay());

        assertEquals(
                LocalDate.of(2026, 8, 19),
                result.bestSalesDay()
        );

        assertEquals(
                75L,
                result.bestSalesDayOrders()
        );
    }


    // 7 BEST REVENUE DAY


    @Test
    void generateReportData_shouldReturnBestRevenueDay() {

        Object[] bestDay = {
                java.sql.Date.valueOf("2026-08-20"),
                new BigDecimal("450000")
        };

        when(orderAnalyticsRepository.getTotalRevenue(
                startDateTime, endDateTime
        )).thenReturn(new BigDecimal("1000000"));

        when(orderAnalyticsRepository.getTotalProfit(
                startDateTime, endDateTime
        )).thenReturn(new BigDecimal("300000"));

        when(orderAnalyticsRepository.countOrders(
                startDateTime, endDateTime
        )).thenReturn(200L);

        when(orderAnalyticsRepository.totalProductsSold(
                startDateTime, endDateTime
        )).thenReturn(300L);

        when(orderAnalyticsRepository.countDistinctProducts(
                startDateTime, endDateTime
        )).thenReturn(10L);

        when(orderAnalyticsRepository.findBestRevenueDay(
                startDateTime,
                endDateTime
        )).thenReturn(Collections.singletonList(bestDay));

        mockEmptyQueriesExceptBestRevenueDay();

        ReportDataDTO result =
                analyticsService.generateReportData(start, end);

        assertNotNull(result.bestRevenueDay());

        assertEquals(
                LocalDate.of(2026, 8, 20),
                result.bestRevenueDay()
        );

        assertEquals(
                new BigDecimal("450000"),
                result.bestRevenueDayAmount()
        );
    }


    //AUXILIARES

    private void mockEmptyQueries() {

        when(orderAnalyticsRepository.findTopProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findRevenueByWeek(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findProfitByWeek(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findOrdersByDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findOrdersByHour(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findBestSalesDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findBestRevenueDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findTopLossProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findTopProfitProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(productAnalyticsRepository.movementSummaryByProduct(
                startDateTime, endDateTime
        )).thenReturn(List.of());
    }


    private void mockEmptyQueriesExceptTopProducts() {
        when(orderAnalyticsRepository.findRevenueByWeek(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findProfitByWeek(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findOrdersByDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findOrdersByHour(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findBestSalesDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findBestRevenueDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findTopLossProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findTopProfitProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(productAnalyticsRepository.movementSummaryByProduct(
                startDateTime, endDateTime
        )).thenReturn(List.of());
    }


    private void mockEmptyQueriesExceptRevenue() {
        when(orderAnalyticsRepository.findTopProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findProfitByWeek(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findOrdersByDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findOrdersByHour(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findBestSalesDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findBestRevenueDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findTopLossProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findTopProfitProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(productAnalyticsRepository.movementSummaryByProduct(
                startDateTime, endDateTime
        )).thenReturn(List.of());
    }


    private void mockEmptyQueriesExceptProfit() {
        when(orderAnalyticsRepository.findTopProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findRevenueByWeek(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findOrdersByDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findOrdersByHour(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findBestSalesDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findBestRevenueDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findTopLossProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findTopProfitProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(productAnalyticsRepository.movementSummaryByProduct(
                startDateTime, endDateTime
        )).thenReturn(List.of());
    }


    private void mockEmptyQueriesExceptOrdersByDay() {
        when(orderAnalyticsRepository.findTopProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findRevenueByWeek(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findProfitByWeek(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findOrdersByHour(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findBestSalesDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findBestRevenueDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findTopLossProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findTopProfitProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(productAnalyticsRepository.movementSummaryByProduct(
                startDateTime, endDateTime
        )).thenReturn(List.of());
    }


    private void mockEmptyQueriesExceptOrdersByHour() {
        when(orderAnalyticsRepository.findTopProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findRevenueByWeek(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findProfitByWeek(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findOrdersByDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findBestSalesDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findBestRevenueDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findTopLossProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findTopProfitProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(productAnalyticsRepository.movementSummaryByProduct(
                startDateTime, endDateTime
        )).thenReturn(List.of());
    }


    private void mockEmptyQueriesExceptBestSalesDay() {
        when(orderAnalyticsRepository.findTopProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findRevenueByWeek(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findProfitByWeek(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findOrdersByDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findOrdersByHour(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findBestRevenueDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findTopLossProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findTopProfitProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(productAnalyticsRepository.movementSummaryByProduct(
                startDateTime, endDateTime
        )).thenReturn(List.of());
    }


    private void mockEmptyQueriesExceptBestRevenueDay() {
        when(orderAnalyticsRepository.findTopProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findRevenueByWeek(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findProfitByWeek(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findOrdersByDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findOrdersByHour(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findBestSalesDay(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findTopLossProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(orderAnalyticsRepository.findTopProfitProducts(
                startDateTime, endDateTime
        )).thenReturn(List.of());

        when(productAnalyticsRepository.movementSummaryByProduct(
                startDateTime, endDateTime
        )).thenReturn(List.of());
    }
}
