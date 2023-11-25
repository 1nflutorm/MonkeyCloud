package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.dtos.SessionControl;
import com.monkeyteam.monkeycloud.dtos.UserDto;
import com.monkeyteam.monkeycloud.dtos.authDtos.RegistrationUserDto;
import com.monkeyteam.monkeycloud.dtos.jwtDtos.JwtRequest;
import com.monkeyteam.monkeycloud.dtos.jwtDtos.JwtResponse;
import com.monkeyteam.monkeycloud.entities.Folder;
import com.monkeyteam.monkeycloud.entities.RefreshToken;
import com.monkeyteam.monkeycloud.entities.User;
import com.monkeyteam.monkeycloud.exeptions.AppError;
import com.monkeyteam.monkeycloud.repositories.FolderRepository;
import com.monkeyteam.monkeycloud.repositories.UserRepository;
import com.monkeyteam.monkeycloud.utils.JwtTokenUtils;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final RefreshTokenService refreshTokenService;
    private final MinioService minioService;

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
        try {
            minioService.createBucket(user.getUsername());
        } catch (ServerException | InsufficientDataException | ErrorResponseException |
                 IOException | NoSuchAlgorithmException | InvalidKeyException |
                 InvalidResponseException | XmlParserException | InternalException e) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка создания бакета"), HttpStatus.BAD_REQUEST);
        }
        Folder folder = new Folder();
        folder.setUserId(user.getUser_id());
        folder.setFolderPath("Minio");
        folder.setFolderName(user.getUsername());
        folder.setFolderAccess(1);
        folderRepository.save(folder);
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
