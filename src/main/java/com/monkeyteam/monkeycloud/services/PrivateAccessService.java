package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.dtos.GrantAccessDto;
import com.monkeyteam.monkeycloud.dtos.ListOfData;
import com.monkeyteam.monkeycloud.dtos.MinioDto;
import com.monkeyteam.monkeycloud.dtos.PrivateAccessDto;
import com.monkeyteam.monkeycloud.dtos.fileDtos.GetFilesRequest;
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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrivateAccessService {

    private UserRepository userRepository;
    private FolderRepository folderRepository;
    private PrivateAccessRepository privateAccessRepository;
    private TelegramRepository telegramRepository;

    private FileService fileService;

    private BotService botService;

    private PublicAccessService publicAccessService;

    @Autowired
    public void setPublicAccessService(PublicAccessService publicAccessService){
        this.publicAccessService = publicAccessService;
    }

    @Autowired
    public void setFileService(FileService fileService){
        this.fileService = fileService;
    }
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

    public ResponseEntity<?> getFilesInPrivateFolder(String owner, String username, Long folderId){
        Optional<User> user = userRepository.findByUsername(username);
        Optional<Folder> folder = folderRepository.findFolderByFolderId(folderId);
        if(user.isEmpty() || folder.isEmpty()){
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Пользователя или папки не существует"), HttpStatus.BAD_REQUEST);
        }

        if(folder.get().getFolderAccess() == 1){
            return new ResponseEntity<>(new AppError(HttpStatus.FORBIDDEN.value(), "У Вас нет доступа к этой папке"), HttpStatus.FORBIDDEN);
        }

        PrivateAccessEntity privateAccess = new PrivateAccessEntity();
        privateAccess.setFolderId(folderId);
        privateAccess.setUserId(user.get().getUser_id());
        Optional<PrivateAccessEntity> privateAccessEntity = privateAccessRepository.findById(privateAccess);

        List<MinioDto> fileList = null;
        if(folderRepository.findFolderByFolderId(folderId).get().getFolderAccess() == 2) {
            if (privateAccessEntity.isEmpty()) {
                return new ResponseEntity<>(new AppError(HttpStatus.FORBIDDEN.value(), "У Вас нет доступа к этой папке"), HttpStatus.FORBIDDEN);
            }
        } else {
            fileList = fileService.getUserFiles(new GetFilesRequest(owner, folder.get().getFolderPath()));
        }
        return ResponseEntity.ok(new ListOfData(fileList));
    }

    public ResponseEntity<?> getPrivateAccess(PrivateAccessDto privateAccessDto) {
        String botUrl = "http://localhost:7070/get-access";
        String customerUsername = privateAccessDto.getCustomer();
        String ownerUsername = privateAccessDto.getOwner();
        Long folderIdd = privateAccessDto.getFolderId();
        String fullPath = folderRepository.findFolderByFolderId(folderIdd).get().getFolderPath();

        Optional<User> optionalCustomer = userRepository.findByUsername(customerUsername);
        Optional<User> optionalOwner = userRepository.findByUsername(ownerUsername);
        if (optionalCustomer.isEmpty() || optionalOwner.isEmpty())
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "такого пользователя не существует"), HttpStatus.BAD_REQUEST);
        Long customerId = optionalCustomer.get().getUser_id();
        Long ownerId = optionalOwner.get().getUser_id();

        Optional<Folder> folder = folderRepository.findFolderByUserIdAndPath(ownerId, fullPath);

        Long parentFolderId = publicAccessService.getParent(folder.get().getFolderId());
        if(parentFolderId != -1){
            Folder parentFolder = folderRepository.findFolderByFolderId(parentFolderId).get();
            if(parentFolder.getFolderAccess() == 3){
                return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Вы не можете открыть приватный доступ к этой папке, т.к папка верхнего уровня имеет общий доступ"), HttpStatus.BAD_REQUEST);
            }
        }

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
        try {
            folderRepository.setFolderAccess(2, grantAccessDto.getFolderID());
        } catch (RuntimeException e) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Вы не можете открыть приватный доступ к этой папке, т.к папка верхнего уровня имеет общий доступ"), HttpStatus.BAD_REQUEST);
        }
        privateAccessRepository.save(privateAccessEntity);
        return ResponseEntity.ok("Доступ открыт");
    }
}
