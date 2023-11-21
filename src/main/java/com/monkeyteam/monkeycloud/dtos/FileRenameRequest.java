package com.monkeyteam.monkeycloud.dtos;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
@Data
public class FileRenameRequest {
    private String username;
    private String fullPath;
    private String oldName;
    private String newName;
}
