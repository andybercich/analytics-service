package org.example.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.Model.Enum.ReportType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class GeneratedReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reportName;

    @Enumerated(EnumType.STRING)
    private ReportType reportType;

    private String filePath;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private LocalDateTime generatedAt;

    @PrePersist
    void onCreate() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }
}
