package org.example.Service;


import lombok.RequiredArgsConstructor;
import org.example.Events.StockMovementType;
import org.example.Exception.AIServiceException;
import org.example.Exception.ReportGenerationException;
import org.example.Model.*;
import org.example.Model.DtoAndRecords.*;
import org.example.Model.Enum.AnalyticsProductType;
import org.example.Model.Enum.ReportType;
import org.example.Repository.GeneratedReportRepository;
import org.example.Repository.MonthlyAnalyticsRepository;
import org.example.Repository.MonthlyProductAnalyticsRepository;
import org.example.Service.Interfaces.ReportStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final AnalyticsServiceImpl analyticsService;
    private final ChartServiceImpl chartService;
    private final PdfServiceImpl pdfService;
    private final ReportStorageService storageService;
    private final GeneratedReportRepository reportRepository;
    private final AIAnalyticsServiceImpl aiAnalyticsService;
    private final MonthlyAnalyticsRepository monthlyAnalyticsRepository;
    private final MonthlyProductAnalyticsRepository monthlyProductAnalyticsRepository;

    @Transactional
    public GeneratedReportDTO generateMonthlyReport(LocalDate month) {

        log.info("Starting monthly report generation. month={}", month);

        try {
            LocalDate start = month.withDayOfMonth(1);
            LocalDate end = month.withDayOfMonth(month.lengthOfMonth());

            log.info("Generating analytics data. start={}, end={}", start, end);

            ReportDataDTO data = analyticsService.generateReportData(start, end);

            saveMonthlyAnalytics(month, data);
            saveMonthlyProductAnalytics(month, data);

            log.info("Generating report charts. month={}", month);

            List<File> charts = List.of(
                    chartService.createTopProductsChart(
                            data.topProducts()),

                    chartService.createRevenueProfitChart(
                            data.revenueByWeek(),
                            data.profitByWeek()),

                    chartService.createStockMovementChart(
                            data.stockMovements()),

                    chartService.createTopProfitProductsChart(
                            data.topProfitProducts()),

                    chartService.createOrdersByDayChart(
                            data.dailyOrders()),

                    chartService.createOrdersByHourChart(
                            data.ordersByHour())
            );

            log.info("Generating monthly PDF. month={}, charts={}", month, charts.size());

            File pdf = pdfService.generatePdf(data, charts, null);

            String path = storageService.saveReport(pdf, ReportType.MONTHLY);

            GeneratedReport report = new GeneratedReport();
            report.setFilePath(path);
            report.setReportType(ReportType.MONTHLY);
            report.setReportName("Reporte mensual %d-%02d".formatted(month.getYear(), month.getMonthValue()));
            report.setPeriodStart(start);
            report.setPeriodEnd(end);
            report.setGeneratedAt(LocalDateTime.now());

            GeneratedReport savedReport = reportRepository.save(report);

            log.info("Monthly report generated successfully. month={}, reportId={}",
                    month, savedReport.getId());

            return GeneratedReportDTO.from(savedReport);

        } catch (Exception ex) {
            log.error("Error generating monthly report. month={}", month, ex);
            throw new ReportGenerationException(
                    "Error al generar el reporte mensual para el año " + month, ex
            );
        }
    }

    @Transactional
    public GeneratedReportDTO generateAIMonthlyReport(LocalDate month) {

        log.info("Starting AI monthly report generation. month={}", month);

        try {
            LocalDate start = month.withDayOfMonth(1);
            LocalDate end = month.withDayOfMonth(month.lengthOfMonth());

            log.info("Generating analytics data for AI report. start={}, end={}",
                    start, end);

            ReportDataDTO data =
                    analyticsService.generateReportData(start, end);

            MonthlyAnalytics monthlyAnalytics =
                    saveMonthlyAnalytics(month, data);

            List<MonthlyProductAnalytics> monthlyProducts =
                    saveMonthlyProductAnalytics(month, data);

            log.info("Generating report charts. month={}", month);

            List<File> charts = List.of(
                    chartService.createTopProductsChart(
                            data.topProducts()
                    ),

                    chartService.createRevenueProfitChart(
                            data.revenueByWeek(),
                            data.profitByWeek()
                    ),

                    chartService.createStockMovementChart(
                            data.stockMovements()
                    ),

                    chartService.createTopProfitProductsChart(
                            data.topProfitProducts()
                    ),

                    chartService.createOrdersByDayChart(
                            data.dailyOrders()
                    ),

                    chartService.createOrdersByHourChart(
                            data.ordersByHour())
            );

            log.info("Generating AI insights. month={}", month);

            AIReportInsights aiInsights;

            try {
                aiInsights = aiAnalyticsService.generateMonthlyInsights(
                        data,
                        monthlyProducts
                );

            }catch (Exception ex){
                log.error(
                        "AI service unavailable. Generating report without AI insights. month={}",
                        month,
                        ex
                );

                aiInsights = AIReportInsights.fallback();
            }

            log.info("Generating AI monthly PDF. month={}", month);

            File pdf = pdfService.generatePdf(data, charts, aiInsights);

            String path = storageService.saveReport(pdf, ReportType.MONTHLY);

            GeneratedReport report = new GeneratedReport();
            report.setFilePath(path);
            report.setReportType(ReportType.MONTHLY);
            report.setReportName("Reporte mensual con IA %d-%02d".formatted(month.getYear(), month.getMonthValue()));
            report.setPeriodStart(start);
            report.setPeriodEnd(end);
            report.setGeneratedAt(LocalDateTime.now());

            GeneratedReport savedReport = reportRepository.save(report);

            log.info("AI monthly report generated successfully. month={}, reportId={}",
                    month, savedReport.getId());

            return GeneratedReportDTO.from(savedReport);

        } catch (Exception ex) {
            log.error("Error generating AI monthly report. month={}", month, ex);
            throw new AIServiceException(
                    "Error al generar el reporte mensual para el año " + month, ex
            );
        }
    }

    public GeneratedReportDTO generateAnnualReport(int year) {

        log.info("Starting annual report generation. year={}", year);

        try {
            List<MonthlyAnalytics> monthlyAnalytics =
                    monthlyAnalyticsRepository.findByYearOrderByMonthAsc(year);

            List<MonthlyProductAnalytics> monthlyProducts =
                    monthlyProductAnalyticsRepository
                            .findByYearOrderByMonthAscRankingAsc(year);

            if (monthlyAnalytics.isEmpty()) {
                log.warn("No analytics data found for annual report. year={}", year);

                throw new RuntimeException(
                        "No existen datos de analytics para el año " + year
                );
            }

            log.info("Generating annual AI data. year={}", year);

            AnnualAIData annualAIData =
                    analyticsService.generateAnnualAIData(year);

            log.info("Generating annual AI insights. year={}", year);

            AIAnnualReportInsights aiInsights;

            try {

                aiInsights =
                        aiAnalyticsService.generateAnnualInsights(annualAIData);


            }catch (Exception ex){
                log.error(
                        "AI service unavailable. Generating report without AI insights. year={}",
                        year,
                        ex
                );

                aiInsights = AIAnnualReportInsights.fallback();
            }


            log.info("Generating annual PDF. year={}", year);

            File pdf =
                    pdfService.generateAnnualPdf(annualAIData, aiInsights);

            String path = storageService.saveReport(pdf, ReportType.ANNUAL);

            GeneratedReport report = new GeneratedReport();
            report.setFilePath(path);
            report.setReportType(ReportType.MONTHLY);
            report.setReportName("Reporte anual con IA %d".formatted(year));
            report.setPeriodStart(LocalDate.now());
            report.setPeriodEnd(LocalDate.now());
            report.setGeneratedAt(LocalDateTime.now());

            GeneratedReport savedReport = reportRepository.save(report);


            log.info("Annual report generated successfully. year={}", year);

            return GeneratedReportDTO.from(savedReport);

        } catch (Exception ex) {
            log.error("Error generating annual report. year={}", year, ex);
            throw new ReportGenerationException(
                    "Error al generar el reporte anual para el año " + year, ex
            );
        }
    }

    @Transactional
    private MonthlyAnalytics saveMonthlyAnalytics(
            LocalDate month,
            ReportDataDTO data) {

        MonthlyAnalytics analytics = monthlyAnalyticsRepository
                .findByYearAndMonth(month.getYear(), month.getMonthValue())
                .orElseGet(MonthlyAnalytics::new);

        analytics.setYear(month.getYear());
        analytics.setMonth(month.getMonthValue());

        analytics.setTotalRevenue(data.totalRevenue());
        analytics.setTotalProfit(data.totalProfit());
        analytics.setProfitMargin(data.profitMargin());

        analytics.setTotalOrders(data.totalOrders());
        analytics.setTotalProductsSold(data.totalProductsSold());
        analytics.setDistinctProducts(data.distinctProducts());
        analytics.setAverageOrderValue(data.averageOrderValue());

        analytics.setBestSalesDay(data.bestSalesDay());
        analytics.setBestSalesDayOrders(data.bestSalesDayOrders());

        analytics.setBestRevenueDay(data.bestRevenueDay());
        analytics.setBestRevenueDayAmount(data.bestRevenueDayAmount());

        analytics.setTotalUnitsLost(
                data.stockMovements().stream()
                        .filter(m -> m.movementType() == StockMovementType.LOSS)
                        .mapToLong(StockMovementSummaryDTO::quantity)
                        .sum()
        );

        analytics.setGeneratedAt(LocalDateTime.now());

        analytics.setOrdersByHour(
                data.ordersByHour() != null
                        ? new ArrayList<>(data.ordersByHour())
                        : new ArrayList<>()
        );

        return monthlyAnalyticsRepository.save(analytics);
    }

    @Transactional
    private List<MonthlyProductAnalytics> saveMonthlyProductAnalytics(
            LocalDate month,
            ReportDataDTO data) {

        int year = month.getYear();
        int monthNumber = month.getMonthValue();

        monthlyProductAnalyticsRepository
                .deleteByYearAndMonth(year, monthNumber);

        monthlyProductAnalyticsRepository.flush();

        List<MonthlyProductAnalytics> entities = new ArrayList<>();

        for (int i = 0; i < data.topProducts().size(); i++) {

            TopProductDTO product = data.topProducts().get(i);

            MonthlyProductAnalytics analytics =
                    new MonthlyProductAnalytics();

            analytics.setYear(year);
            analytics.setMonth(monthNumber);
            analytics.setProductId(product.productId());
            analytics.setProductName(product.productName());
            analytics.setQuantitySold(product.totalQuantity());
            analytics.setRevenue(product.totalRevenue());
            analytics.setRanking(i + 1);
            analytics.setType(AnalyticsProductType.TOP_SELLER);

            entities.add(analytics);
        }

        for (int i = 0; i < data.topProfitProducts().size(); i++) {

            ProductProfitDTO product =
                    data.topProfitProducts().get(i);

            MonthlyProductAnalytics analytics =
                    new MonthlyProductAnalytics();

            analytics.setYear(year);
            analytics.setMonth(monthNumber);
            analytics.setProductId(product.productId());
            analytics.setProductName(product.productName());
            analytics.setProfit(product.totalProfit());
            analytics.setRanking(i + 1);
            analytics.setType(AnalyticsProductType.TOP_PROFIT);

            entities.add(analytics);
        }

        for (int i = 0; i < data.topLossProducts().size(); i++) {

            ProductLossDTO product =
                    data.topLossProducts().get(i);

            MonthlyProductAnalytics analytics =
                    new MonthlyProductAnalytics();

            analytics.setYear(year);
            analytics.setMonth(monthNumber);
            analytics.setProductId(product.productId());
            analytics.setProductName(product.productName());
            analytics.setQuantityLost(product.totalLost());
            analytics.setRanking(i + 1);
            analytics.setType(AnalyticsProductType.TOP_LOSS);

            entities.add(analytics);
        }

        log.info(
                "Saving monthly product analytics. year={}, month={}, records={}",
                year,
                monthNumber,
                entities.size()
        );

        return monthlyProductAnalyticsRepository.saveAll(entities);
    }

    @Transactional(readOnly = true)
    public List<GeneratedReportDTO> findAll() {
        log.info("Fetching all generated reports");
        return reportRepository.findAll().stream()
                .map(GeneratedReportDTO::from)
                .toList();
    }
}