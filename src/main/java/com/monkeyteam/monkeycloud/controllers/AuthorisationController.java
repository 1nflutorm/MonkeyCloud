package com.monkeyteam.monkeycloud.controllers;

import com.monkeyteam.monkeycloud.dtos.TelegramDto;
import com.monkeyteam.monkeycloud.dtos.jwtDtos.JwtRequest;
import com.monkeyteam.monkeycloud.dtos.authDtos.RegistrationUserDto;
import com.monkeyteam.monkeycloud.entities.RefreshToken;
import com.monkeyteam.monkeycloud.exeptions.AppError;
import com.monkeyteam.monkeycloud.exeptions.RefreshTokenExeption;
import com.monkeyteam.monkeycloud.services.AuthService;
import com.monkeyteam.monkeycloud.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthorisationController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/sign-up")
    public ResponseEntity<?> registration(@RequestBody RegistrationUserDto registrationUserDto) {
        ResponseEntity<?> responseEntity = authService.createNewUser(registrationUserDto);
        if (responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Пользователь с указанным именем уже существует"), HttpStatus.BAD_REQUEST);
        }
        ResponseEntity<?> token = authService.authorize(new JwtRequest(registrationUserDto.getUsername(), registrationUserDto.getPassword()));
        return token;
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> login(@RequestBody JwtRequest authRequest) {
        return authService.authorize(authRequest);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<?> signout(@RequestHeader HttpHeaders headers) {
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
        return authService.signout(authHeader);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader HttpHeaders headers) {
        RefreshToken refreshToken = null;
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
        try{
            refreshToken = refreshTokenService.getRefreshToken(authHeader);
            refreshTokenService.verifyToken(refreshToken);
        } catch (RefreshTokenExeption e) {
            refreshTokenService.setSessionInactive(authHeader);
            return new ResponseEntity<>(new AppError(HttpStatus.REQUEST_TIMEOUT.value(), e.getMessage()), HttpStatus.REQUEST_TIMEOUT);
        }
        return authService.createTokens(refreshTokenService.getUsername(refreshToken.getUser_id()));
    }

    @PostMapping("/addTelegramId")
    public ResponseEntity<?> addTelegramId(@RequestBody TelegramDto telegramDto){
        if(authService.notifiesTheBot(telegramDto).getStatusCode().value() != 200){
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка добавлния telegramId"), HttpStatus.BAD_REQUEST);
        }
        return authService.addTelegramId(telegramDto);
    }


}
