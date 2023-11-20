package com.monkeyteam.monkeycloud.dtos;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
@Data
public class FileUploadRequest {
    private String username;
    private MultipartFile multipartFile;
}
