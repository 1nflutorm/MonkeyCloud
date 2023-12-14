package com.monkeyteam.monkeycloud.dtos.searchDtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrivateSearchByDate {
    private String username;
    private String date;
}