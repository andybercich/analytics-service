import org.example.Model.DtoAndRecords.*;
import org.example.Service.PdfServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfServiceImplTest {

    private PdfServiceImpl pdfService;

    private final File reportsDir = new File("reports");
    private final File chartsDir = new File("charts");

    @BeforeEach
    void setUp() {
        pdfService = new PdfServiceImpl();
    }

    @AfterEach
    void tearDown() {

        deleteDirectory(reportsDir);
        deleteDirectory(chartsDir);
    }

    @Test
    void shouldGeneratePdfSuccessfully() {

        ReportDataDTO data = createReportData();

        List<File> charts = List.of(
                createFakeChart("revenue_by_week.png"),
                createFakeChart("profit_by_week.png"),
                createFakeChart("stock_movements.png"),
                createFakeChart("top_products.png"),
                createFakeChart("orders_by_day.png"),
                createFakeChart("orders_by_hour.png")
        );

        AIReportInsights insights = createAIInsights();

        File result = pdfService.generatePdf(
                data,
                charts,
                insights
        );

        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.isFile());
        assertTrue(result.length() > 0);
        assertTrue(result.getName().endsWith(".pdf"));
    }

    @Test
    void shouldGeneratePdfWithoutCharts() {

        ReportDataDTO data = createReportData();

        File result = pdfService.generatePdf(
                data,
                List.of(),
                null
        );

        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.isFile());
        assertTrue(result.length() > 0);
    }

    @Test
    void shouldGeneratePdfWithNullCharts() {

        ReportDataDTO data = createReportData();

        File result = pdfService.generatePdf(
                data,
                null,
                null
        );

        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.length() > 0);
    }

    @Test
    void shouldGeneratePdfWithAIInsights() {

        ReportDataDTO data = createReportData();

        AIReportInsights insights = createAIInsights();

        File result = pdfService.generatePdf(
                data,
                List.of(),
                insights
        );

        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.length() > 0);
    }

    @Test
    void shouldGeneratePdfWithEmptyReportData() {

        ReportDataDTO data = new ReportDataDTO(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0L,
                0L,
                0L,
                null,
                0L,
                null,
                BigDecimal.ZERO,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        File result = pdfService.generatePdf(
                data,
                List.of(),
                null
        );

        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.length() > 0);
    }

    private ReportDataDTO createReportData() {

        return new ReportDataDTO(
                BigDecimal.valueOf(1000000),
                BigDecimal.valueOf(300000),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(50000),
                20L,
                50L,
                8L,
                java.time.LocalDate.of(2026, 8, 19),
                10L,
                java.time.LocalDate.of(2026, 8, 19),
                BigDecimal.valueOf(250000),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private AIReportInsights createAIInsights() {

        return new AIReportInsights(
                "El período presentó un buen desempeño comercial.",
                "Los ingresos muestran una tendencia positiva.",
                "La rentabilidad se mantiene estable.",
                "Se recomienda controlar las pérdidas de stock.",
                "El Notebook presenta un buen rendimiento.",
                "Los pedidos presentan picos horarios definidos.",
                List.of(
                        "Incrementar disponibilidad de los productos más rentables.",
                        "Analizar las pérdidas de inventario.",
                        "Optimizar la planificación en horarios de alta demanda."
                )
        );
    }
    private File createFakeChart(String fileName) {

        try {
            if (!chartsDir.exists()) {
                assertTrue(chartsDir.mkdirs());
            }

            File file = new File(chartsDir, fileName);

            BufferedImage image = new BufferedImage(
                    1000,
                    650,
                    BufferedImage.TYPE_INT_RGB
            );

            Graphics2D graphics = image.createGraphics();

            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, 1000, 650);

            graphics.setColor(Color.BLACK);
            graphics.drawString("Test Chart", 50, 50);

            graphics.dispose();

            boolean created = ImageIO.write(
                    image,
                    "png",
                    file
            );

            assertTrue(created);
            assertTrue(file.exists());
            assertTrue(file.length() > 0);

            return file;

        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el gráfico de prueba", e);
        }
    }

    private void deleteDirectory(File directory) {

        if (!directory.exists()) {
            return;
        }

        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }

        directory.delete();
    }
}
