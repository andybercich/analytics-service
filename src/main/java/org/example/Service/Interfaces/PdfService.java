package org.example.Service.Interfaces;

import org.example.Model.DtoAndRecords.AIAnnualReportInsights;
import org.example.Model.DtoAndRecords.AIReportInsights;
import org.example.Model.DtoAndRecords.AnnualAIData;
import org.example.Model.DtoAndRecords.ReportDataDTO;

import java.io.File;
import java.util.List;

public interface PdfService {

    File generatePdf(ReportDataDTO data, List<File> charts, AIReportInsights aiInsights);

    File generateAnnualPdf(AnnualAIData annualData, AIAnnualReportInsights aiInsights);

}
