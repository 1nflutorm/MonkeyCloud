package com.monkeyteam.monkeycloud.dtos.fileDtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetFilesRequest {
    String username;
    String folder;
}
