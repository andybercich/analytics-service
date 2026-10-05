package org.example.Model.DtoAndRecords;

import org.example.Model.Enum.ReportType;
import org.example.Model.GeneratedReport;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GeneratedReportDTO(
        Long id,
        String reportName,
        ReportType reportType,
        LocalDate periodStart,
        LocalDate periodEnd,
        LocalDateTime generatedAt
) {
    public static GeneratedReportDTO from(GeneratedReport report) {
        return new GeneratedReportDTO(
                report.getId(),
                report.getReportName(),
                report.getReportType(),
                report.getPeriodStart(),
                report.getPeriodEnd(),
                report.getGeneratedAt()
        );
    }
}
