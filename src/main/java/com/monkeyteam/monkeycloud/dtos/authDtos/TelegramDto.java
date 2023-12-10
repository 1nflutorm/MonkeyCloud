package com.monkeyteam.monkeycloud.dtos.authDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TelegramDto {
    private Long telegramId;
    private String username;
}
