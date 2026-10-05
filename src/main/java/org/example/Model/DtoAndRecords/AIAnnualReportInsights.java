package org.example.Model.DtoAndRecords;

import java.util.List;

public record AIAnnualReportInsights(
        String executiveSummary,
        String annualPerformance,
        String bestAndWorstMonths,
        String topProducts,
        String profitabilityAnalysis,
        String lossAnalysis,
        String recurringProducts,
        String salesVsProfitability,
        String trends,
        List<String> recommendations
) {
    public static AIAnnualReportInsights fallback() {
        return new AIAnnualReportInsights(
                "Los insights generados mediante inteligencia artificial no están disponibles actualmente.",
                "No fue posible generar un análisis del rendimiento anual mediante inteligencia artificial.",
                "No fue posible generar un análisis de los mejores y peores meses.",
                "No fue posible generar un análisis de los productos destacados.",
                "No fue posible generar un análisis de rentabilidad.",
                "No fue posible generar un análisis de pérdidas.",
                "No fue posible generar un análisis de productos recurrentes.",
                "No fue posible generar un análisis de ventas y rentabilidad.",
                "No fue posible generar un análisis de tendencias.",
                List.of("Intente generar el análisis nuevamente más tarde.")
        );
    }
}
