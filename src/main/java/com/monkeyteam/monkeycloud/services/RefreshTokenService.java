package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.dtos.RefreshRequest;
import com.monkeyteam.monkeycloud.entities.RefreshToken;
import com.monkeyteam.monkeycloud.entities.User;
import com.monkeyteam.monkeycloud.exeptions.RefreshTokenExeption;
import com.monkeyteam.monkeycloud.repositories.RefreshTokenRepository;
import com.monkeyteam.monkeycloud.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService{
    private long REFRESH_TOKEN_LIFETIME = 864000000;//10 дней
    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    public void setUserRepository (UserRepository userRepository){
        this.userRepository = userRepository;
    }
    @Autowired
    public void setRefreshTokenRepository(RefreshTokenRepository refreshTokenRepository){
        this.refreshTokenRepository = refreshTokenRepository;
    }
    public RefreshToken generateRefreshToken(String username) {
        User user = null;
        RefreshToken refreshToken = new RefreshToken();
        Optional<User> optUser = userRepository.findByUsername(username);
        if (optUser.isPresent()) {
            user = optUser.get();
        }
        refreshToken.setUser_id(user.getUser_id());
        refreshToken.setExpiryDate(Instant.now().plusMillis(REFRESH_TOKEN_LIFETIME));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyToken(RefreshToken token) throws RefreshTokenExeption{
        Instant expiryDate = token.getExpiryDate();
        String refreshToken = token.getToken();
        if(!refreshTokenRepository.findByToken(refreshToken).isPresent()){
            throw new RefreshTokenExeption(token.getToken(), "Токен отсутствует в базе данных");//подделан рефреш токен
        }
        if(expiryDate.isBefore(Instant.now())){
            throw new RefreshTokenExeption(token.getToken(), "Рефреш токен протух");// время жизни истекло
        }
        refreshTokenRepository.deleteById(token.getToken());
        return token;
    }

    public void deleteUserById(long id){
        refreshTokenRepository.deleteByUserId(id);
    }
    public String getUsername(long user_id){
        User user = userRepository.findById(user_id).get();
        return user.getUsername();
    }

    public RefreshToken getRefreshToken (RefreshRequest request) throws RefreshTokenExeption {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(request.getToken());
        if (!refreshToken.isPresent()){
            throw new RefreshTokenExeption(request.getToken(), "Токен отсутствует в базе данных");
        }
        return refreshToken.get();
    }
}
