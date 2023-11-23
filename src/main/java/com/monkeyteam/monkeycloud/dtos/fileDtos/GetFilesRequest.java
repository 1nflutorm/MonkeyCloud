package com.monkeyteam.monkeycloud.dtos.fileDtos;

import lombok.Data;

@Data
public class GetFilesRequest {
    String username;
    String folder;
}
