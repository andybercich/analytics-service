import org.example.Events.StockMovementType;
import org.example.Model.DtoAndRecords.*;
import org.example.Model.HourlyOrder;
import org.example.Service.ChartServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChartServiceImplTest {

    private ChartServiceImpl chartService;

    private final File chartsDir = new File("charts");

    @BeforeEach
    void setUp() {
        chartService = new ChartServiceImpl();
    }

    @AfterEach
    void tearDown() {
        if (chartsDir.exists()) {
            File[] files = chartsDir.listFiles();

            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }

            chartsDir.delete();
        }
    }

    @Test
    void shouldCreateTopProductsChart() {

        List<TopProductDTO> data = List.of(
                new TopProductDTO(
                        1L,
                        "Notebook Lenovo",
                        10L,
                        BigDecimal.valueOf(150000)
                ),
                new TopProductDTO(
                        2L,
                        "SSD NVMe",
                        7L,
                        BigDecimal.valueOf(80000)
                )
        );

        File result = chartService.createTopProductsChart(data);

        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.isFile());
        assertTrue(result.getName().equals("top_products.png"));
    }

    @Test
    void shouldCreateTopProductsChartWithNullData() {

        File result = chartService.createTopProductsChart(null);

        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.isFile());
    }

    @Test
    void shouldCreateStockMovementChart() {


        List<StockMovementSummaryDTO> data = List.of(
                new StockMovementSummaryDTO(
                        20L,
                        "Coca Cero",
                        StockMovementType.RESTOCK,
                        4L
                ),
                new StockMovementSummaryDTO(
                        21L,
                        "Fanta",
                        StockMovementType.LOSS,
                        2L
                ),
                new StockMovementSummaryDTO(
                        22L,
                        "Pepsi",
                        StockMovementType.RETURN,
                        6L
                )

        );

        File result = chartService.createStockMovementChart(data);

        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.isFile());
        assertEquals("stock_movements.png", result.getName());
    }

    @Test
    void shouldCreateStockMovementChartWithEmptyData() {

        File result = chartService.createStockMovementChart(List.of());

        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.isFile());
    }

    @Test
    void shouldCreateRevenueProfitChart() {

        List<WeeklyRevenueDTO> revenue = List.of(
                new WeeklyRevenueDTO(
                        java.time.LocalDate.of(2026, 8, 1),
                        BigDecimal.valueOf(500000)
                ),
                new WeeklyRevenueDTO(
                        java.time.LocalDate.of(2026, 8, 8),
                        BigDecimal.valueOf(750000)
                )
        );

        List<WeeklyProfitDTO> profit = List.of(
                new WeeklyProfitDTO(
                        java.time.LocalDate.of(2026, 8, 1),
                        BigDecimal.valueOf(150000)
                ),
                new WeeklyProfitDTO(
                        java.time.LocalDate.of(2026, 8, 8),
                        BigDecimal.valueOf(230000)
                )
        );

        File result = chartService.createRevenueProfitChart(
                revenue,
                profit
        );

        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.isFile());
        assertEquals("revenue_vs_profit.png", result.getName());
    }

    @Test
    void shouldCreateTopProfitProductsChart() {

        List<ProductProfitDTO> data = List.of(
                new ProductProfitDTO(
                        1L,
                        "Notebook Lenovo",
                        BigDecimal.valueOf(170000)
                ),
                new ProductProfitDTO(
                        2L,
                        "SSD NVMe",
                        BigDecimal.valueOf(38000)
                )
        );

        File result = chartService.createTopProfitProductsChart(data);

        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.isFile());
        assertEquals("top_profit_products.png", result.getName());
    }

    @Test
    void shouldCreateOrdersByDayChart() {

        List<DailyOrdersDTO> data = List.of(
                new DailyOrdersDTO(
                        java.time.LocalDate.of(2026, 8, 19),
                        25L
                ),
                new DailyOrdersDTO(
                        java.time.LocalDate.of(2026, 8, 20),
                        18L
                )
        );

        File result = chartService.createOrdersByDayChart(data);

        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.isFile());
        assertEquals("orders_by_day.png", result.getName());
    }

    @Test
    void shouldCreateOrdersByHourChart() {

        List<HourlyOrder> data = List.of(
                new HourlyOrder(9, 10L),
                new HourlyOrder(10, 15L),
                new HourlyOrder(14, 20L)
        );

        File result = chartService.createOrdersByHourChart(data);

        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.isFile());
        assertEquals("orders_by_hour", result.getName());
    }
}
