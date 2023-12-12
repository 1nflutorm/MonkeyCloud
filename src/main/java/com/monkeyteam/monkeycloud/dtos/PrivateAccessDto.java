package com.monkeyteam.monkeycloud.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PrivateAccessDto {
    private String owner;
    private String customer;
    private Long folderId;
}
