package org.example.Service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.example.Exception.ReportGenerationException;
import org.example.Model.DtoAndRecords.*;
import org.example.Service.Interfaces.PdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.List;

@Service
public class PdfServiceImpl implements PdfService {

    private final static Logger log = LoggerFactory.getLogger(PdfServiceImpl.class);

    @Override
    public File generateAnnualPdf(AnnualAIData annualData, AIAnnualReportInsights aiInsights) {

        log.info("Starting annual PDF generation. year={}", annualData.year());

        File pdfFile = null;
        try {
            pdfFile = Files.createTempFile("reporte_anual_" + annualData.year() + "_", ".pdf").toFile();

            try (PdfWriter writer = new PdfWriter(pdfFile.getAbsolutePath());
                 PdfDocument pdfDoc = new PdfDocument(writer);
                 Document document = new Document(pdfDoc, PageSize.A4)) {

                document.setMargins(35, 40, 35, 40);

                addAnnualHeader(document, annualData.year());
                addAnnualSummary(document, annualData);
                addAnnualProducts(document, annualData);
                addAnnualAIAnalysis(document, aiInsights);

                document.add(new Paragraph("Fin del reporte")
                        .setFontSize(9)
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(25));
            }

            log.info("Annual PDF generated successfully. year={}, file={}",
                    annualData.year(), pdfFile.getAbsolutePath());

            return pdfFile;

        } catch (IOException | RuntimeException  ex) {

            log.error("Error generating annual PDF. year={}",
                    annualData.year(), ex);

            throw new ReportGenerationException("Error al generar el PDF anual", ex);
        }
    }

    @Override
    public File generatePdf(ReportDataDTO data, List<File> charts, AIReportInsights aiInsights) {

        log.info("Starting monthly PDF generation. charts={}, aiAnalysis={}",
                charts != null ? charts.size() : 0,
                aiInsights != null);

        File pdfFile = null;
        try {
            pdfFile = Files.createTempFile("reporte_mensual_", ".pdf").toFile();

            try (PdfWriter writer = new PdfWriter(pdfFile.getAbsolutePath());
                 PdfDocument pdfDoc = new PdfDocument(writer);
                 Document document = new Document(pdfDoc, PageSize.A4)) {

                document.setMargins(35, 40, 35, 40);

                addHeader(document);
                addSummary(document, data, aiInsights);
                addTopProductsSection(document, data);
                addTopProfitProductsSection(document, data);
                addTopLossProductsSection(document, data);
                addCharts(document, charts, aiInsights, data);
                addAIRecommendations(document, aiInsights != null ? aiInsights.recommendations() : null);
                addFooter(document);
            }

            log.info("Monthly PDF generated successfully. file={}", pdfFile.getAbsolutePath());
            return pdfFile;

        } catch (IOException | RuntimeException ex) {
            if (pdfFile != null) {
                pdfFile.delete();
            }
            log.error("Error generating monthly PDF", ex);
            throw new ReportGenerationException("Error al generar el PDF mensual", ex);
        }
    }
    // Métodos auxiliares principales MES
    private void addHeader(Document document) {

        Paragraph title = new Paragraph("REPORTE MENSUAL")
                .setFontSize(24)
                .setBold()
                .setMarginBottom(3);
        document.add(title);

        Paragraph subtitle = new Paragraph("Reporte de Órdenes, Ventas e Inventario")
                .setFontSize(12)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginBottom(5);
        document.add(subtitle);

        Paragraph generated = new Paragraph("Generado automáticamente por el sistema")
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setMarginBottom(20);
        document.add(generated);

    }

    private void addSummary(Document document, ReportDataDTO data, AIReportInsights aiInsights) {

        document.add(createSectionTitle("Resumen del período"));
        Table kpiTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}));
        kpiTable.setWidth(UnitValue.createPercentValue(100));
        addKpi(kpiTable, "INGRESOS", formatMoney(data.totalRevenue()));
        addKpi(kpiTable, "GANANCIAS", formatMoney(data.totalProfit()));
        addKpi(kpiTable, "ÓRDENES", String.valueOf(data.totalOrders()));
        addKpi(kpiTable, "PRODUCTOS VENDIDOS", String.valueOf(data.totalProductsSold()));
        addKpi(kpiTable, "PRODUCTOS ÚNICOS", String.valueOf(data.distinctProducts()));
        addKpi(kpiTable, "MARGEN", calculateMargin(data.totalRevenue(), data.totalProfit()));
        document.add(kpiTable);

        if (aiInsights != null) {
            addAIInsight(document, "Análisis ejecutivo", aiInsights.executiveSummary());
        }

    }

    private void addTopProductsSection(Document document, ReportDataDTO data) {

        Div section = new Div();
        section.setKeepTogether(true);

        section.add(createSectionTitle("Productos más vendidos"));
        Table table = new Table(
                UnitValue.createPercentArray(new float[]{0.15f, 0.55f, 0.30f})
        );

        table.setWidth(UnitValue.createPercentValue(100));


        addHeaderCell(table, "#");
        addHeaderCell(table, "Producto");
        addHeaderCell(table, "Cantidad");

        int position = 1;

        for (TopProductDTO product : data.topProducts()) {
            table.addCell(new Cell().add(
                    new Paragraph(String.valueOf(position++))
            ));
            table.addCell(new Cell().add(
                    new Paragraph(product.productName())
            ));
            table.addCell(new Cell().add(
                    new Paragraph(String.valueOf(product.totalQuantity()))
            ));
        }
        section.add(table);
        document.add(section);

    }

    private void addTopProfitProductsSection(Document document, ReportDataDTO data) {

        if (data.topProfitProducts() != null && !data.topProfitProducts().isEmpty()) {

            document.add(createSectionTitle("Productos con mayores ganancias"));
            Table table = new Table(UnitValue.createPercentArray(new float[]{0.55f, 0.45f}));
            table.setWidth(UnitValue.createPercentValue(100));
            addHeaderCell(table, "Producto");
            addHeaderCell(table, "Ganancia");

            for (ProductProfitDTO product : data.topProfitProducts()) {
                table.addCell(new Cell().add(new Paragraph(product.productName())));
                table.addCell(new Cell().add(new Paragraph(formatMoney(product.totalProfit()))));
            }
            document.add(table);
        }

    }

    private void addTopLossProductsSection(Document document, ReportDataDTO data) {
        if (data.topLossProducts() != null && !data.topLossProducts().isEmpty()) {
            document.add(createSectionTitle("Productos con mayores pérdidas"));
            Table table = new Table(UnitValue.createPercentArray(new float[]{0.55f, 0.45f}));
            table.setWidth(UnitValue.createPercentValue(100));
            addHeaderCell(table, "Producto");
            addHeaderCell(table, "Pérdida");

            for (ProductLossDTO product : data.topLossProducts()) {
                table.addCell(new Cell().add(new Paragraph(product.productName())));
                table.addCell(new Cell().add(new Paragraph(formatMoney(product.totalLost()))));
            }
            document.add(table);
        }
    }

    private void addCharts(Document document, List<File> charts, AIReportInsights aiInsights, ReportDataDTO data) throws MalformedURLException {

        addChartSection(document, "Ingresos vs Ganancias", findChart(charts, "revenue_vs_profit"));

        if (aiInsights != null) {
            addAIInsight(document, "Análisis de ingresos", aiInsights.salesInsight());
            addAIInsight(document, "Análisis de rentabilidad", aiInsights.profitabilityInsight());
        }

        addChartSection(document, "Movimientos de stock", findChart(charts, "stock_movements"));
        if (aiInsights != null) {
            addAIInsight(document, "Análisis de stock", aiInsights.stockInsight());
        }

        addChartSection(document, "Top productos", findChart(charts, "top_products"));
        addChartSection(document, "Productos con mayor ganancia", findChart(charts, "top_profit_products"));

        if (aiInsights != null) {
            addAIInsight(document, "Análisis de productos", aiInsights.productInsight());
        }

        addChartSection(document, "Pedidos por día", findChart(charts, "orders_by_day"));
        if (data.bestSalesDay() != null) {
            document.add(new Paragraph("Día con mayor cantidad de pedidos: " + data.bestSalesDay() + " (" + data.bestSalesDayOrders() + " pedidos)")
                    .setFontSize(10)
                    .setBold()
                    .setMarginBottom(8));
        }

        addChartSection(document, "Pedidos por horario", findChart(charts, "orders_by_hour"));

        if (data.bestRevenueDay() != null) {
            document.add(new Paragraph("Día con mayor facturación: " + data.bestRevenueDay() + " (" + formatMoney(data.bestRevenueDayAmount()) + ")")
                    .setFontSize(10)
                    .setBold()
                    .setMarginBottom(8));
        }
        if (aiInsights != null) {
            addAIInsight(document, "Análisis de pedidos", aiInsights.orderPatternInsight());
        }
    }

    private void addFooter(Document document) {
        document.add(new Paragraph("Fin del reporte")
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(25));
    }

// Métodos auxiliares principales AÑO
    private void addAnnualHeader(Document document, Integer year) {

        document.add(new Paragraph("REPORTE ANUAL").setFontSize(24).setBold().setMarginBottom(3));

        document.add(new Paragraph("Análisis de rendimiento del año " + year)
                .setFontSize(12)
                .setFontColor(ColorConstants.DARK_GRAY).setMarginBottom(5));

        document.add(new Paragraph("Generado automáticamente por el sistema")
                .setFontSize(9).setFontColor(ColorConstants.GRAY).setMarginBottom(20));
    }

    private void addAnnualSummary(Document document, AnnualAIData annualData) {

        document.add(createSectionTitle("Evolución mensual"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{0.15f, 0.25f, 0.25f, 0.20f, 0.15f}));

        table.setWidth(UnitValue.createPercentValue(100));
        addHeaderCell(table, "Mes");
        addHeaderCell(table, "Ingresos");
        addHeaderCell(table, "Ganancias");
        addHeaderCell(table, "Pedidos");
        addHeaderCell(table, "Margen");

        for (MonthlySummaryAI month : annualData.monthly()) {

            table.addCell(new Cell().add(new Paragraph(String.valueOf(month.month()))));
            table.addCell(new Cell().add(new Paragraph(formatMoney(month.totalRevenue()))));
            table.addCell(new Cell().add(new Paragraph(formatMoney(month.totalProfit()))));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(month.totalOrders()))));
            table.addCell(new Cell().add(new Paragraph(calculateMargin(month.totalRevenue(),
                    month.totalProfit()))));

        }

        document.add(table);
    }

    private void addAnnualProducts(Document document, AnnualAIData annualData) {
        document.add(createSectionTitle("Rendimiento anual por producto"));
        Table table = new Table(UnitValue.createPercentArray(new float[]{0.30f, 0.15f, 0.18f, 0.18f, 0.19f}));

        table.setWidth(UnitValue.createPercentValue(100));
        addHeaderCell(table, "Producto");
        addHeaderCell(table, "Vendidos");
        addHeaderCell(table, "Ingresos");
        addHeaderCell(table, "Ganancia");
        addHeaderCell(table, "Pérdidas");

        for (ProductAnnualSummaryAI product : annualData.products()) {

            table.addCell(new Cell().add(new Paragraph(product.productName())));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(product.totalQuantitySold()))));
            table.addCell(new Cell().add(new Paragraph(formatMoney(product.totalRevenue()))));
            table.addCell(new Cell().add(new Paragraph(formatMoney(product.totalProfit()))));
            table.addCell(new Cell().add(new Paragraph(formatMoney(product.totalLost()))));

        }
        document.add(table);
    }

    private void addAnnualAIAnalysis(Document document, AIAnnualReportInsights insights) {
        if (insights == null) return;

        addAIInsight(document, "Resumen ejecutivo", insights.executiveSummary());
        addAIInsight(document, "Rendimiento anual", insights.annualPerformance());
        addAIInsight(document, "Mejores y peores meses", insights.bestAndWorstMonths());
        addAIInsight(document, "Productos destacados", insights.topProducts());
        addAIInsight(document, "Análisis de rentabilidad", insights.profitabilityAnalysis());
        addAIInsight(document, "Análisis de pérdidas", insights.lossAnalysis());
        addAIInsight(document, "Productos recurrentes", insights.recurringProducts());
        addAIInsight(document, "Ventas vs rentabilidad", insights.salesVsProfitability());
        addAIInsight(document, "Tendencias", insights.trends());
        addAIRecommendations(document, insights.recommendations());
    }

    // Métodos auxiliares generales
    private void addAIInsight(Document document, String title, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        document.add(createSectionTitle(title));
        document.add(new Paragraph(text)
                .setFontSize(10)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setMarginBottom(15));
    }

    private void addAIRecommendations(Document document, List<String> recommendations) {
        if (recommendations == null || recommendations.isEmpty()) {
            return;
        }
        document.add(createSectionTitle("Recomendaciones"));
        for (String recommendation : recommendations) {
            if (recommendation == null || recommendation.isBlank()) {
                continue;
            }
            document.add(new Paragraph("• " + recommendation)
                    .setFontSize(10)
                    .setMarginBottom(6));
        }
    }

    private Paragraph createSectionTitle(String title) {
        return new Paragraph(title)
                .setFontSize(15)
                .setBold()
                .setMarginTop(12)
                .setMarginBottom(8);
    }

    private void addKpi(Table table, String title, String value) {
        Cell cell = new Cell();
        cell.setPadding(10);
        cell.add(new Paragraph(title)
                .setFontSize(8)
                .setBold()
                .setFontColor(ColorConstants.GRAY));
        cell.add(new Paragraph(value)
                .setFontSize(14)
                .setBold()
                .setMarginTop(3));
        table.addCell(cell);
    }

    private void addHeaderCell(Table table, String text) {
        Cell cell = new Cell();
        cell.add(new Paragraph(text)
                .setBold()
                .setFontSize(9));
        cell.setPadding(7);
        table.addCell(cell);
    }


    private void addChartSection(Document document, String title, File chart)
            throws MalformedURLException {

        if (chart == null || !chart.exists()) {
            return;
        }

        Div section = new Div();
        section.setKeepTogether(true);

        section.add(createSectionTitle(title));

        Image img = new Image(
                ImageDataFactory.create(chart.getAbsolutePath())
        );

        img.setWidth(UnitValue.createPercentValue(95));
        img.setAutoScaleHeight(true);
        img.setHorizontalAlignment(HorizontalAlignment.CENTER);

        section.add(img);

        document.add(section);
    }

    private File findChart(List<File> charts, String key) {
        if (charts == null) {
            return null;
        }
        return charts.stream()
                .filter(f -> f.getName().startsWith("chart_" + key + "_"))
                .findFirst()
                .orElse(null);
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "$0.00";
        }
        return "$" + value.setScale(2, RoundingMode.HALF_UP);
    }
    private String calculateMargin(BigDecimal revenue, BigDecimal profit) {
        if (revenue == null || revenue.compareTo(BigDecimal.ZERO) == 0) {
            return "0%";
        }
        BigDecimal margin = profit.divide(revenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        return margin.setScale(1, RoundingMode.HALF_UP) + "%";
    }
}
