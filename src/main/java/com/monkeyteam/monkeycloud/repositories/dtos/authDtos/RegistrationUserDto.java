package com.monkeyteam.monkeycloud.repositories.dtos.authDtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegistrationUserDto {
    private String username;
    private String password;
    private String second_name;
    private String name;
    private String last_name;
}
