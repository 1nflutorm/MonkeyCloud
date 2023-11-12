package com.monkeyteam.monkeycloud.exeptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenExeption extends Throwable{
    private String token;
    private String message;
}
