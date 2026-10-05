package org.example.Service;


import org.example.Model.Enum.ReportType;
import org.example.Service.Interfaces.ReportStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.util.UUID;

@Service
@Profile("aws")
public class S3ReportStorageService implements ReportStorageService {

    private static final Logger log = LoggerFactory.getLogger(S3ReportStorageService.class);

    private final S3Client s3Client;

    @Value("${report.storage.s3.bucket}")
    private String bucket;

    public S3ReportStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String saveReport(File pdf, ReportType reportType) {
        String key = "reports/%s/%s.pdf".formatted(reportType.name(), UUID.randomUUID());
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType("application/pdf")
                            .build(),
                    RequestBody.fromFile(pdf));
            log.info("Report saved to S3. type={}, key={}", reportType, key);
            return key;
        } catch (SdkException ex) {
            throw new RuntimeException("Error al guardar el reporte", ex);
        }
    }

    @Override
    public Resource getReport(String storageKey) {
        try {
            byte[] bytes = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucket).key(storageKey).build()
            ).asByteArray();
            return new ByteArrayResource(bytes);
        } catch (NoSuchKeyException ex) {
            throw new RuntimeException("Reporte no encontrado", ex);
        } catch (SdkException ex) {
            throw new RuntimeException("Error al obtener el reporte", ex);
        }
    }

    @Override
    public void deleteReport(String storageKey) {
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder().bucket(bucket).key(storageKey).build());
        } catch (SdkException ex) {
            throw new RuntimeException("Error al eliminar el reporte", ex);
        }
    }
}