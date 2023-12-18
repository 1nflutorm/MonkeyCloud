package com.monkeyteam.monkeycloud.configs;

import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.hibernate.tool.schema.internal.exec.ScriptTargetOutputToFile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {
    //cd c:\minio
    //.\minio.exe server C:\minio --console-address :9090

    private final MinioProperties minioProperties;
    @Bean
    public MinioClient configuredMinioClient() {

        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getUrl())
                .credentials(minioProperties.getAccessKey(),minioProperties.getSecretKey())
                .build();

        try{
            minioClient.makeBucket(MakeBucketArgs.builder().bucket("test").build());
            System.out.println("CREATED");
        } catch (Exception e) {
            System.out.println("NOT CREATED");
            return minioClient;
        }

        System.out.println(minioProperties.getUrl());
        return minioClient;
    }
}