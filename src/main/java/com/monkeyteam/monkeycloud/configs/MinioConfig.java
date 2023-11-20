package com.monkeyteam.monkeycloud.configs;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {
    //cd c:\minio
    //.\minio.exe server C:\minio --console-address :9090
    @Bean
    public MinioClient configuredMinioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("admin","password")
                .build();
        return minioClient;
    }
}