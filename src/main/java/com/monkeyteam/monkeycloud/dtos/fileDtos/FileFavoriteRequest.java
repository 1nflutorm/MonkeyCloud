package com.monkeyteam.monkeycloud.dtos.fileDtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileFavoriteRequest {
    private String username;
    private String fullPath;
}