package com.monkeyteam.monkeycloud.dtos.fileDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
@Data
@AllArgsConstructor
public class FileUploadRequest {
    private String username;
    private String fullPath;
    private MultipartFile multipartFile;
}
