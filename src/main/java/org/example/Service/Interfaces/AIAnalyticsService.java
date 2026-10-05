package org.example.Service.Interfaces;

import org.example.Model.DtoAndRecords.AIAnnualReportInsights;
import org.example.Model.DtoAndRecords.AIReportInsights;
import org.example.Model.DtoAndRecords.AnnualAIData;
import org.example.Model.DtoAndRecords.ReportDataDTO;
import org.example.Model.MonthlyAnalytics;
import org.example.Model.MonthlyProductAnalytics;

import java.util.List;

public interface AIAnalyticsService {

    AIReportInsights generateMonthlyInsights(ReportDataDTO monthlyAnalytics,
                                             List<MonthlyProductAnalytics> productAnalytics);


    public AIAnnualReportInsights generateAnnualInsights(AnnualAIData annualAIData);
}
