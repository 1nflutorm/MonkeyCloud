package com.monkeyteam.monkeycloud.dtos.jwtDtos;

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
    private String role;
    private Long size;
}
