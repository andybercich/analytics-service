package org.example.Controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.example.Model.DtoAndRecords.GeneratedReportDTO;
import org.example.Model.DtoAndRecords.ReportDataDTO;
import org.example.Model.GeneratedReport;
import org.example.Service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/analytics/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/generate/monthly")
    @RateLimiter(name = "report")
    public ResponseEntity<GeneratedReportDTO> generateMonthly(
            @RequestParam("year") int year,
            @RequestParam("month") int month) {

        LocalDate monthDate = LocalDate.of(year, month, 1);

        GeneratedReportDTO report = reportService.generateMonthlyReport(monthDate);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(report);
    }

    @PostMapping("/generate/ia/monthly")
    @RateLimiter(name = "aiReport")
    public ResponseEntity<GeneratedReportDTO> generateIAMonthly(
            @RequestParam("year") int year,
            @RequestParam("month") int month) {

        LocalDate monthDate = LocalDate.of(year, month, 1);

        GeneratedReportDTO report = reportService.generateAIMonthlyReport(monthDate);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(report);
    }

    @GetMapping("/generate/ia/year")
    @RateLimiter(name = "aiReport")
    public ResponseEntity<GeneratedReportDTO> downloadReport(
            @RequestParam("year") int year) {

        GeneratedReportDTO reportFile = reportService.generateAnnualReport(year);

        return ResponseEntity.ok(reportFile);
    }

    @GetMapping
    @RateLimiter(name = "report")
    public ResponseEntity<List<GeneratedReportDTO>> getReports() {
        List<GeneratedReportDTO> reports = reportService.findAll();

        return ResponseEntity.ok(reports);

    }
}