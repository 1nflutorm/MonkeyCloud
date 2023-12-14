package com.monkeyteam.monkeycloud.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GrantAccessDto {
    private Long telegramID;
    private Long customerID;
    private Long folderID;
}
