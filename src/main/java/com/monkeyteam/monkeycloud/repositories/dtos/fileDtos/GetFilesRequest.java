package com.monkeyteam.monkeycloud.repositories.dtos.fileDtos;

import lombok.Data;

@Data
public class GetFilesRequest {
    String username;
    String folder;
}
