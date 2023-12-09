package com.monkeyteam.monkeycloud.dtos.folderDtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OpenFolderRequest {
    private String username;
    private Long folderId;
}
