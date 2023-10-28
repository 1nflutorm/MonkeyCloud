package com.monkeyteam.monkeycloud.controllers;

import com.monkeyteam.monkeycloud.dtos.JwtRequest;
import com.monkeyteam.monkeycloud.dtos.RegistrationUserDto;
import com.monkeyteam.monkeycloud.exeptions.AppError;
import com.monkeyteam.monkeycloud.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthorisationController {
    private final AuthService authService;

    @PostMapping("/registration")
    public ResponseEntity<?> registration(@RequestBody RegistrationUserDto registrationUserDto) {
        ResponseEntity<?> responseEntity = authService.createNewUser(registrationUserDto);
        if (responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Пользователь с указанным именем уже существует"), HttpStatus.BAD_REQUEST);
        }
        JwtRequest jwtRequest = new JwtRequest();
        jwtRequest.setUsername(registrationUserDto.getUsername());
        jwtRequest.setPassword(registrationUserDto.getPassword());
        return authService.createAuthToken(jwtRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtRequest authRequest) {
        return authService.createAuthToken(authRequest);
    }
}
