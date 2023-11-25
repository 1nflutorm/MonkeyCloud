package com.monkeyteam.monkeycloud.repositories.dtos.jwtDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String username;
    private String accessToken;
    private String refreshToken;
}
