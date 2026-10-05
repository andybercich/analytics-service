package org.example.Service;

import lombok.RequiredArgsConstructor;
import org.example.Exception.ReportNotFoundException;
import org.example.Model.DtoAndRecords.GeneratedReportDTO;
import org.example.Model.DtoAndRecords.ReportFile;
import org.example.Model.GeneratedReport;
import org.example.Repository.GeneratedReportRepository;
import org.example.Service.Interfaces.ReportStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportStorageServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(ReportStorageServiceImpl.class);

    private final GeneratedReportRepository reportRepository;
    private final ReportStorageService storageService;

    @Transactional(readOnly = true)
    public Page<GeneratedReportDTO> findAll(Pageable pageable) {
        return reportRepository.findAll(pageable).map(GeneratedReportDTO::from);
    }

    @Transactional(readOnly = true)
    public GeneratedReportDTO findDetail(Long id) {
        return GeneratedReportDTO.from(findById(id));
    }

    @Transactional(readOnly = true)
    public GeneratedReport findById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ReportNotFoundException("Reporte no encontrado. id=" + id));
    }

    public ReportFile download(Long id) {
        GeneratedReport report = findById(id);

        log.info("Downloading report. id={}, key={}", id, report.getFilePath());

        Resource resource = storageService.getReport(report.getFilePath());

        return new ReportFile(buildFileName(report), resource);
    }

    private String buildFileName(GeneratedReport report) {
        String name = report.getReportName() != null
                ? report.getReportName()
                : "reporte " + report.getId();

        return name.endsWith(".pdf") ? name : name + ".pdf";
    }
}
