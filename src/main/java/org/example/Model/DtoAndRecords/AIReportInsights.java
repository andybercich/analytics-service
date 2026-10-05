package org.example.Model.DtoAndRecords;

import java.util.List;

public record AIReportInsights(

        String executiveSummary,

        String salesInsight,

        String profitabilityInsight,

        String stockInsight,

        String productInsight,
        String orderPatternInsight,
        List<String> recommendations

){

    public static AIReportInsights fallback() {
        return new AIReportInsights(
                "Los insights generados mediante inteligencia artificial no están disponibles actualmente.",
                "No fue posible generar un análisis de ventas mediante inteligencia artificial.",
                "No fue posible generar un análisis de rentabilidad mediante inteligencia artificial.",
                "No fue posible generar un análisis de stock mediante inteligencia artificial.",
                "No fue posible generar un análisis de productos mediante inteligencia artificial.",
                "No fue posible generar un análisis de patrones de pedidos mediante inteligencia artificial.",
                List.of("Intente generar el análisis nuevamente más tarde.")
        );
    }
}
