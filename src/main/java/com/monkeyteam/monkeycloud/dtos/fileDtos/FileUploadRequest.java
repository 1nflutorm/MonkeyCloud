package com.monkeyteam.monkeycloud.dtos.fileDtos;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
@Data
public class FileUploadRequest {
    private String username;
    private String fullPath;
    private MultipartFile multipartFile;
}
