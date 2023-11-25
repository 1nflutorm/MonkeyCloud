package com.monkeyteam.monkeycloud.repositories.dtos.folderDtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FolderDeleteRequest {
    private String username;
    private String fullPath;
}
