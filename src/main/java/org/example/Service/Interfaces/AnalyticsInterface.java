package org.example.Service.Interfaces;

import org.example.Model.DtoAndRecords.ReportDataDTO;

import java.time.LocalDate;

public interface AnalyticsInterface {


    ReportDataDTO generateReportData(
            LocalDate start,
            LocalDate end
    );
}
