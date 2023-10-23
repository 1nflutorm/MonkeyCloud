package com.monkeyteam.monkeycloud.controllers;

import com.monkeyteam.monkeycloud.dtos.JwtRequest;
import com.monkeyteam.monkeycloud.dtos.RegistrationUserDto;
import com.monkeyteam.monkeycloud.exeptions.AppError;
import com.monkeyteam.monkeycloud.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequiredArgsConstructor
public class AuthorisationController {

    private final AuthService authService;
    @GetMapping("/login")
    public ModelAndView login(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login.html");
        return modelAndView;
    }

    @GetMapping("/registration")
    public ModelAndView registration(){
        System.out.println("GET запрос страницы регистрации");
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("registration.html");
        return modelAndView;
    }

    @PostMapping("/registration")
    public ResponseEntity<?> registration(@RequestBody RegistrationUserDto registrationUserDto) {
        ResponseEntity<?> responseEntity = authService.createNewUser(registrationUserDto);
        if(responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Пользователь с указанным именем уже существует"), HttpStatus.BAD_REQUEST);
        }
        JwtRequest jwtRequest = new JwtRequest();
        jwtRequest.setUsername(registrationUserDto.getUsername());
        jwtRequest.setPassword(registrationUserDto.getPassword());
        return authService.createAuthToken(jwtRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthToken(@RequestBody JwtRequest authRequest) {
        return authService.createAuthToken(authRequest);
    }
}
