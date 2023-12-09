package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.dtos.GrantAccessDto;
import com.monkeyteam.monkeycloud.dtos.PrivateAccessDto;
import com.monkeyteam.monkeycloud.entities.Folder;
import com.monkeyteam.monkeycloud.entities.PrivateAccessEntity;
import com.monkeyteam.monkeycloud.entities.TelegramUser;
import com.monkeyteam.monkeycloud.entities.User;
import com.monkeyteam.monkeycloud.exeptions.AppError;
import com.monkeyteam.monkeycloud.repositories.FolderRepository;
import com.monkeyteam.monkeycloud.repositories.PrivateAccessRepository;
import com.monkeyteam.monkeycloud.repositories.TelegramRepository;
import com.monkeyteam.monkeycloud.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.sql.SQLException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrivateAccessService {

    private UserRepository userRepository;
    private FolderRepository folderRepository;
    private PrivateAccessRepository privateAccessRepository;
    private TelegramRepository telegramRepository;

    private BotService botService;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setFolderRepository(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    @Autowired
    public void setPrivateAccessRepository(PrivateAccessRepository privateAccessRepository) {
        this.privateAccessRepository = privateAccessRepository;
    }

    @Autowired
    public void setBotService(BotService botService) {
        this.botService = botService;
    }

    @Autowired
    public void setTelegramRepository(TelegramRepository telegramRepository) {
        this.telegramRepository = telegramRepository;
    }

    public ResponseEntity<?> getPrivateAccess(PrivateAccessDto privateAccessDto) {
        String botUrl = "http://localhost:7070/get-access";
        String customerUsername = privateAccessDto.getCustomer();
        String ownerUsername = privateAccessDto.getOwner();
        String fullPath = privateAccessDto.getFullPath();

        Optional<User> optionalCustomer = userRepository.findByUsername(customerUsername);
        Optional<User> optionalOwner = userRepository.findByUsername(ownerUsername);
        if (optionalCustomer.isEmpty() || optionalOwner.isEmpty())
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "такого пользователя не существует"), HttpStatus.BAD_REQUEST);
        Long customerId = optionalCustomer.get().getUser_id();
        Long ownerId = optionalOwner.get().getUser_id();

        Optional<TelegramUser> optionalTelegramUser = telegramRepository.findByUserId(ownerId);
        if (optionalTelegramUser.isEmpty()) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "такого пользователя телеграмма не существует"), HttpStatus.BAD_REQUEST);
        }
        Long tgId = optionalTelegramUser.get().getChatId();

        Optional<Folder> optionalFolder = folderRepository.findFolderByUserIdAndPath(optionalOwner.get().getUser_id(), fullPath);
        if (optionalFolder.isEmpty())
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "такой папки не существует"), HttpStatus.BAD_REQUEST);
        Long folderId = optionalFolder.get().getFolderId();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(botUrl)
                .queryParam("telegramID", Long.toString(tgId))
                .queryParam("userID", Long.toString(customerId))
                .queryParam("username", customerUsername)
                .queryParam("folderID", Long.toString(folderId))
                .queryParam("folderName", fullPath);

        //ResponseEntity<?> response = botService.sendGetRequestToBot(builder);

        return botService.sendGetRequestToBot(builder);
    }

    public ResponseEntity<?> grantAccess(GrantAccessDto grantAccessDto) {
        PrivateAccessEntity privateAccessEntity = new PrivateAccessEntity();
        privateAccessEntity.setFolderId(grantAccessDto.getFolderID());
        privateAccessEntity.setUserId(grantAccessDto.getCustomerID());
        folderRepository.setFolderAccess(2, grantAccessDto.getFolderID());
        privateAccessRepository.save(privateAccessEntity);
        return ResponseEntity.ok("Доступ открыт");
    }
}
