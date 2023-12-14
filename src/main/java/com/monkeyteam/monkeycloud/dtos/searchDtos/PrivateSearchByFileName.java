package com.monkeyteam.monkeycloud.dtos.searchDtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrivateSearchByFileName {
    private String username;
    private String filename;
}