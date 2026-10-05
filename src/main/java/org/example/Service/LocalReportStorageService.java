package org.example.Service;

import org.example.Model.Enum.ReportType;
import org.example.Service.Interfaces.ReportStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Profile("local")
public class LocalReportStorageService implements ReportStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalReportStorageService.class);

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Value("${report.storage.local.path:./reports}")
    private String storagePath;

    @Override
    public String saveReport(File pdf, ReportType reportType) {
        try {
            Path basePath = basePath();
            Files.createDirectories(basePath);

            String fileName = "%s_%s_%s.pdf".formatted(
                    reportType.name(),
                    LocalDateTime.now().format(TIMESTAMP_FORMAT),
                    UUID.randomUUID().toString().substring(0, 8)
            );

            Path targetPath = resolveSafe(fileName);

            Files.copy(pdf.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Report saved locally. type={}, file={}", reportType, targetPath);

            return fileName;

        } catch (IOException ex) {
            log.error("Error saving report locally. path={}", storagePath, ex);
            throw new RuntimeException("Error al guardar el reporte", ex);
        }
    }

    @Override
    public Resource getReport(String storageKey) {
        Path path = resolveSafe(storageKey);

        Resource resource = new FileSystemResource(path);

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("Reporte no encontrado");
        }

        return resource;
    }

    @Override
    public void deleteReport(String storageKey) {
        try {
            Files.deleteIfExists(resolveSafe(storageKey));
        } catch (IOException ex) {
            log.error("Error deleting report. key={}", storageKey, ex);
            throw new RuntimeException("Error al eliminar el reporte", ex);
        }
    }

    private Path basePath() {
        return Paths.get(storagePath).toAbsolutePath().normalize();
    }

    private Path resolveSafe(String storageKey) {
        Path base = basePath();
        Path path = base.resolve(storageKey).normalize();

        if (!path.startsWith(base)) {
            throw new IllegalArgumentException("Clave de reporte inválida");
        }

        return path;
    }
}
