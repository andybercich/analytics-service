package org.example.Service.Interfaces;

import org.example.Model.Enum.ReportType;
import org.springframework.core.io.Resource;

import java.io.File;

public interface ReportStorageService {

    String saveReport(File pdf, ReportType reportType);

    Resource getReport(String storageKey);

    void deleteReport(String storageKey);
}
