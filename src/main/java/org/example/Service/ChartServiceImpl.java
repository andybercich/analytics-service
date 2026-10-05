package org.example.Service;

import org.example.Exception.GraphicGenerationException;
import org.example.Model.DtoAndRecords.*;
import org.example.Model.HourlyOrder;
import org.example.Service.Interfaces.ChartService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;

@Service
public class ChartServiceImpl implements ChartService {

    private static final Logger logger = LoggerFactory.getLogger(ChartServiceImpl.class);

    @Override
    public File createRevenueProfitChart(List<WeeklyRevenueDTO> revenue, List<WeeklyProfitDTO> profit) {
        try {
            logger.info("Creando gráfico de ingresos vs ganancias...");
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            if (revenue != null) {
                revenue.forEach(dto -> dataset.addValue(dto.revenue(), "Ingresos", dto.weekStart().toString()));
            }

            if (profit != null) {
                profit.forEach(dto -> dataset.addValue(dto.profit(), "Ganancias", dto.weekStart().toString()));
            }

            JFreeChart chart = ChartFactory.createLineChart("Ingresos vs Ganancias", "Semana",
                    "Monto ($)", dataset, PlotOrientation.VERTICAL, true, true, false);
            configureChart(chart);

            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setRangeGridlinePaint(new Color(220, 220, 220));

            LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
            renderer.setDefaultShapesVisible(true);
            renderer.setDefaultShapesFilled(true);
            renderer.setDefaultStroke(new BasicStroke(2.5f));
            renderer.setDefaultItemLabelsVisible(true);

            File chartFile = saveChart(chart, "revenue_vs_profit.png");
            logger.info("Gráfico de ingresos vs ganancias creado exitosamente: {}", chartFile.getAbsolutePath());
            return chartFile;
        } catch (Exception e) {
            logger.error("Error al crear gráfico de ingresos vs ganancias", e);
            throw new GraphicGenerationException("Error al crear gráfico de ingresos vs ganancias", e);
        }
    }

    @Override
    public File createTopProfitProductsChart(List<ProductProfitDTO> data) {
        try {
            logger.info("Creando gráfico de productos con mayor ganancia...");
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            if (data != null) {
                data.forEach(dto -> dataset.addValue(dto.totalProfit(), "Ganancia", dto.productName()));
            }

            JFreeChart chart = ChartFactory.createBarChart("Productos con mayor ganancia", "Producto",
                    "Ganancia ($)", dataset, PlotOrientation.VERTICAL, false, true, false);
            configureChart(chart);

            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setRangeGridlinePaint(new Color(220, 220, 220));

            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setDefaultItemLabelsVisible(true);
            renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());

            File chartFile = saveChart(chart, "top_profit_products.png");
            logger.info("Gráfico de productos con mayor ganancia creado exitosamente: {}", chartFile.getAbsolutePath());
            return chartFile;
        } catch (Exception e) {
            logger.error("Error al crear gráfico de productos con mayor ganancia", e);
            throw new GraphicGenerationException("Error al crear gráfico de productos con mayor ganancia", e);
        }
    }

    @Override
    public File createTopProductsChart(List<TopProductDTO> data) {
        try {
            logger.info("Creando gráfico de productos más vendidos...");
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            if (data != null) {
                data.forEach(dto -> dataset.addValue(dto.totalQuantity(), "Cantidad", dto.productName()));
            }

            JFreeChart chart = ChartFactory.createBarChart("Productos más vendidos",
                    "Producto", "Unidades", dataset,
                    PlotOrientation.VERTICAL, false, true, false);
            configureChart(chart);

            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setRangeGridlinePaint(new Color(220, 220, 220));

            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setDefaultItemLabelsVisible(true);
            renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", NumberFormat.getIntegerInstance()));
            renderer.setMaximumBarWidth(0.15);
            renderer.setItemMargin(0.05);
            renderer.setDrawBarOutline(false);

            File chartFile = saveChart(chart, "top_products.png");
            logger.info("Gráfico de productos más vendidos creado exitosamente: {}", chartFile.getAbsolutePath());
            return chartFile;
        } catch (Exception e) {
            logger.error("Error al crear gráfico de productos más vendidos", e);
            throw new GraphicGenerationException("Error al crear gráfico de productos más vendidos", e);
        }
    }

    @Override
    public File createStockMovementChart(List<StockMovementSummaryDTO> data) {
        try {
            logger.info("Creating graphics with ...");
            DefaultPieDataset dataset = new DefaultPieDataset();

            if (data != null) {
                data.forEach(dto -> {
                    double quantity = Math.abs(dto.quantity());
                    if (quantity > 0) {
                        dataset.setValue(dto.movementType().toString(), quantity);
                    }
                });
            }

            JFreeChart chart = ChartFactory.createPieChart("Movimientos de stock", dataset, true, true, false);
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {2}",
                    NumberFormat.getNumberInstance(), NumberFormat.getPercentInstance()));
            plot.setSimpleLabels(false);

            configureChart(chart);
            File chartFile = saveChart(chart, "stock_movements.png");
            logger.info("Graphic stock movements Successfully create: {}", chartFile.getAbsolutePath());
            return chartFile;
        } catch (Exception e) {
            logger.error("Error creating graphic stock movements", e);
            throw new GraphicGenerationException("Error al crear gráfico de movimientos de stock", e);
        }
    }

    @Override
    public File createOrdersByDayChart(List<DailyOrdersDTO> data) {
        try {
            logger.info("Creating graphic order per day...");
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            if (data != null) {
                data.forEach(dto -> dataset.addValue(dto.orderCount(), "Pedidos", dto.date().toString()));
            }

            JFreeChart chart = ChartFactory.createBarChart("Pedidos por día", "Día",
                    "Cantidad de pedidos", dataset, PlotOrientation.VERTICAL,
                    false, true, false);
            configureChart(chart);

            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setRangeGridlinePaint(new Color(220, 220, 220));

            CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setCategoryLabelPositions(
                    CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4));

            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setDefaultItemLabelsVisible(true);
            renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());

            File chartFile = saveChart(chart, "orders_by_day.png");
            logger.info("Graphic order per day Successfully create: {}", chartFile.getAbsolutePath());
            return chartFile;
        } catch (Exception e) {
            logger.error("Error creating Graphic order per day", e);
            throw new GraphicGenerationException("Error al crear gráfico de pedidos por día", e);
        }
    }

    public File createOrdersByHourChart(List<HourlyOrder> ordersByHour) {
        try {
            logger.info("Creating graphic order by hour...");
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            if (ordersByHour != null) {
                ordersByHour.stream()
                        .sorted(Comparator.comparing(HourlyOrder::getHour))
                        .forEach(dto -> dataset.addValue(dto.getOrderCount(), "Pedidos", String.format("%02d:00", dto.getHour())));
            }

            JFreeChart chart = ChartFactory.createBarChart("Pedidos por horario", "Hora", "Cantidad de pedidos", dataset, PlotOrientation.VERTICAL, true, true, false);
            File chartFile = saveChart(chart, "orders_by_hour");
            logger.info("Graphics order by hour create Successfully: {}", chartFile.getAbsolutePath());
            return chartFile;
        } catch (Exception e) {
            logger.error("Error creating graphics order by hour", e);
            throw new GraphicGenerationException("Error al crear gráfico de pedidos por horario", e);
        }
    }

    private File saveChart(JFreeChart chart, String name) {
        File file = null;
        try {
            String prefix = "chart_" + name.replace(".png", "") + "_";
            file = Files.createTempFile(prefix, ".png").toFile();
            ChartUtils.saveChartAsPNG(file, chart, 1000, 650);
            return file;
        } catch (IOException | RuntimeException e) {
            if (file != null) {
                file.delete();
            }
            logger.error("Error saving graphic: {}", name, e);
            throw new GraphicGenerationException("Error al guardar gráfico: " + name, e);
        }
    }

    private void configureChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 18));
        chart.getTitle().setPaint(new Color(45, 45, 45));

        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 10));
        }

        if (chart.getPlot() instanceof CategoryPlot plot) {
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlineVisible(false);
            plot.setRangeGridlinePaint(new Color(225, 225, 225));
            plot.setDomainGridlinesVisible(false);
        }

        if (chart.getPlot() instanceof PiePlot piePlot) {
            piePlot.setBackgroundPaint(Color.WHITE);
            piePlot.setOutlineVisible(false);
        }
    }
}