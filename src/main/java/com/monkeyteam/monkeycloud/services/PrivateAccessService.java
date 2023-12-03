package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.dtos.PrivateAccessDto;
import com.monkeyteam.monkeycloud.entities.Folder;
import com.monkeyteam.monkeycloud.entities.User;
import com.monkeyteam.monkeycloud.exeptions.AppError;
import com.monkeyteam.monkeycloud.repositories.FolderRepository;
import com.monkeyteam.monkeycloud.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrivateAccessService {

    private UserRepository userRepository;
    private FolderRepository folderRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setFolderRepository(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    public ResponseEntity<?> getPrivateAccess(PrivateAccessDto privateAccessDto) {
        /*
         * TODO: найти tg_id пользователя в БД
         *  */
        String botUrl = "url";
        String customerUsername = privateAccessDto.getCustomer();
        String ownerUsername = privateAccessDto.getOwner();
        String fullPath = privateAccessDto.getFullPath();
        Optional<User> optionalCustomer = userRepository.findByUsername(customerUsername);
        Optional<User> optionalOwner = userRepository.findByUsername(ownerUsername);
        if (optionalCustomer.isEmpty() || optionalOwner.isEmpty())
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "такого пользователя не существует"), HttpStatus.BAD_REQUEST);
        Long userId = optionalCustomer.get().getUser_id();
        Optional<Folder> optionalFolder = folderRepository.findFolderByUserIdAndPath(optionalOwner.get().getUser_id(), fullPath);
        if (optionalFolder.isEmpty())
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "такой папки не существует"), HttpStatus.BAD_REQUEST);
        Long folderId = optionalFolder.get().getFolderId();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(botUrl)
                .queryParam("telegramID", Long.toString(10000000L))
                .queryParam("userID", Long.toString(userId))
                .queryParam("nameUser", customerUsername)
                .queryParam("folderID", Long.toString(folderId))
                .queryParam("folderName", fullPath);

        sendToBot(builder);

        return ResponseEntity.ok("запрос отправлен");
    }

    public void sendToBot(UriComponentsBuilder builder) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "хуй знает что в заголовках");
        //TODO: правльные заголовки
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<?> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);
    }

}
