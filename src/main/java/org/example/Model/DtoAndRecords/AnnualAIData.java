package org.example.Model.DtoAndRecords;

import org.example.Model.MonthlyAnalytics;
import org.example.Model.MonthlyProductAnalytics;

import java.util.List;

public record AnnualAIData(
        Integer year,
        List<MonthlySummaryAI> monthly,
        List<ProductAnnualSummaryAI> products
)
{}
