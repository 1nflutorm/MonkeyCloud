package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.dtos.*;
import com.monkeyteam.monkeycloud.entities.RefreshToken;
import com.monkeyteam.monkeycloud.entities.User;
import com.monkeyteam.monkeycloud.exeptions.AppError;
import com.monkeyteam.monkeycloud.repositories.RefreshTokenRepository;
import com.monkeyteam.monkeycloud.repositories.UserRepository;
import com.monkeyteam.monkeycloud.utils.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    public ResponseEntity<?> authorize(@RequestBody JwtRequest authRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(new AppError(HttpStatus.UNAUTHORIZED.value(), "Неправильный логин или пароль"), HttpStatus.UNAUTHORIZED);
        }
        return createTokens(authRequest.getUsername());
    }
    public ResponseEntity<?> createTokens(@RequestBody String username){
        UserDetails userDetails = userService.loadUserByUsername(username);
        String accessToken = jwtTokenUtils.generateToken(userDetails);
        userRepository.setSessionActive(username);
        RefreshToken refreshToken = refreshTokenService.generateRefreshToken(username);
        return ResponseEntity.ok(new JwtResponse(username, accessToken, refreshToken.getToken()));
    }

    public ResponseEntity<?> createNewUser(@RequestBody RegistrationUserDto registrationUserDto) {
        if (userService.findByUsername(registrationUserDto.getUsername()).isPresent()) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Пользователь с указанным именем уже существует"), HttpStatus.BAD_REQUEST);
        }
        User user = userService.createNewUser(registrationUserDto);
        return ResponseEntity.ok(new UserDto(user.getUser_id(), user.getUsername()));
    }

    public ResponseEntity<?> signout(String authHeaders){
        String token = authHeaders.substring(7);
        String username = jwtTokenUtils.getUsername(token);
        Optional<User> optUser = userRepository.findByUsername(username);
        if(optUser.isPresent()){
            refreshTokenService.deleteUserById(optUser.get().getUser_id());
        }
        userRepository.setSessionInactive(username);
        return ResponseEntity.ok(new SessionControl(username, false));
    }
}
