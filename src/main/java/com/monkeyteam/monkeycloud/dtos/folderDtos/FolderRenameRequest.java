package com.monkeyteam.monkeycloud.dtos.folderDtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FolderRenameRequest {
    private String username;
    private String fullPath;
    private String oldName;
    private String newName;
}
