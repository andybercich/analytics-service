package org.example.Controller;

import lombok.RequiredArgsConstructor;
import org.example.Model.DtoAndRecords.GeneratedReportDTO;
import org.example.Model.DtoAndRecords.ReportFile;
import org.example.Service.ReportStorageServiceImpl;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/analytics/reports/data")
@RequiredArgsConstructor
public class ReportDataController {

    private final ReportStorageServiceImpl reportQueryService;

    @GetMapping
    public Page<GeneratedReportDTO> list(
            @PageableDefault(size = 20, sort = "generatedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return reportQueryService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public GeneratedReportDTO detail(@PathVariable Long id) {
        return reportQueryService.findDetail(id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        ReportFile file = reportQueryService.download(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(file.fileName(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .body(file.resource());
    }
}
