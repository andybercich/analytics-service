package org.example.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@Profile("aws")
public class S3Config {

    @Bean
    public S3Client s3Client(@Value("${report.storage.s3.region}") String region) {

        return S3Client.builder()
                .region(Region.of(region))
                .build();
    }
}
