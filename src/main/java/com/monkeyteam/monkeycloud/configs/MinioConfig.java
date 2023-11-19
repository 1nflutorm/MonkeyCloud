package com.monkeyteam.monkeycloud.configs;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    @Bean
    public MinioClient configuredMinioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint("http://192.168.0.3:51148/")
                .credentials("admin","password")
                .build();
        return minioClient;
    }
}