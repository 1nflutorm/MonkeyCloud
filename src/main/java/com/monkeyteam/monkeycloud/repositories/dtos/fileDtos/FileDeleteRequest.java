package com.monkeyteam.monkeycloud.repositories.dtos.fileDtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileDeleteRequest {
    private String username;
    private String fullPath;
}