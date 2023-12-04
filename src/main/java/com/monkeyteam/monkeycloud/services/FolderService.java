package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.dtos.folderDtos.FolderDeleteRequest;
import com.monkeyteam.monkeycloud.dtos.folderDtos.FolderFavoriteRequest;
import com.monkeyteam.monkeycloud.dtos.folderDtos.FolderRenameRequest;
import com.monkeyteam.monkeycloud.dtos.folderDtos.FolderUploadRequest;
import com.monkeyteam.monkeycloud.dtos.MinioDto;
import com.monkeyteam.monkeycloud.entities.FavoriteFolder;
import com.monkeyteam.monkeycloud.entities.Folder;
import com.monkeyteam.monkeycloud.entities.User;
import com.monkeyteam.monkeycloud.exeptions.AppError;
import com.monkeyteam.monkeycloud.repositories.FavoriteFolderRepository;
import com.monkeyteam.monkeycloud.repositories.FolderRepository;
import com.monkeyteam.monkeycloud.repositories.UserRepository;
import com.monkeyteam.monkeycloud.utils.FileAndFolderUtil;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FolderService {
    private FileService fileService;
    private MinioClient minioClient;
    private FileAndFolderUtil fileAndFolderUtil;
    private FolderRepository folderRepository;
    private FavoriteFolderRepository favoriteFolderRepository;

    private UserRepository userRepository;

    @Autowired
    public void setFolderRepository(FolderRepository folderRepository){
        this.folderRepository = folderRepository;
    }

    @Autowired
    public void setFavoriteFolderRepository(FavoriteFolderRepository favoriteFolderRepository){
        this.favoriteFolderRepository = favoriteFolderRepository;
    }

    @Autowired
    public void setMinioClient(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Autowired
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    @Autowired
    public void setFileAndFolderUtil(FileAndFolderUtil fileAndFolderUtil) {
        this.fileAndFolderUtil = fileAndFolderUtil;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    private List<DeleteObject> convertToDeleteObjects(List<MinioDto> files) {
        List<DeleteObject> objects = new ArrayList<>();
        for (MinioDto file : files) {
            objects.add(new DeleteObject(file.getPath()));
        }
        return objects;
    }

    private List<SnowballObject> convertToSnowballObjects(FolderUploadRequest folderUploadRequest) throws IOException {
        List<SnowballObject> objects = new ArrayList<>();
        for (MultipartFile file : folderUploadRequest.getFiles()) {
            if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
                continue;
            }
            SnowballObject snowballObject = new SnowballObject(
                    folderUploadRequest.getFullPath() + file.getOriginalFilename(),
                    file.getInputStream(),
                    file.getSize(),
                    null);
            objects.add(snowballObject);
        }
        return objects;
    }

    public ResponseEntity<?> uploadFolder(FolderUploadRequest folderUploadRequest) {
        try {
            List<SnowballObject> snowballObject = convertToSnowballObjects(folderUploadRequest);
            minioClient.uploadSnowballObjects(UploadSnowballObjectsArgs
                    .builder()
                    .bucket(folderUploadRequest.getUsername())
                    .objects(snowballObject)
                    .build());
            fileAndFolderUtil.addDirsToDataBaseFromUploadedFolder(snowballObject, folderUploadRequest.getUsername());

        } catch (Exception e) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при загрузке папки"), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("Папка загрузилась корректно");
    }

    public ResponseEntity<?> renameFolder(FolderRenameRequest folderRenameRequest) {
        List<MinioDto> files = null;
        String username = folderRenameRequest.getUsername();
        String fullPath = folderRenameRequest.getFullPath();
        String oldName = folderRenameRequest.getOldName();
        String newName = folderRenameRequest.getNewName();
        try {
            files = fileService.getAllUserFiles(username, fullPath + oldName);
        } catch (Exception e) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при переименовании папки"), HttpStatus.BAD_REQUEST);
        }
        for (MinioDto file : files) {
            try {
                int start = (fullPath + oldName).length();
                String path = file.getPath();
                path = path.substring(start);
                path = fullPath + newName + path;

                String first = fullPath + newName + "/" + file.getPath();
                String sec = file.getPath();
                minioClient.copyObject(CopyObjectArgs
                        .builder()
                        .bucket(username)
                        .object(path)
                        .source(CopySource.builder()
                                .bucket(username)
                                .object(file.getPath())
                                .build())
                        .build());
            } catch (Exception e) {
                return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при переименовании папки"), HttpStatus.BAD_REQUEST);
            }
        }
        FolderDeleteRequest folderDeleteRequest = new FolderDeleteRequest(username, fullPath + oldName);
        deleteFolder(folderDeleteRequest);
        Optional<User> optionalUser = userRepository.findByUsername(username);

        String folderPath = fullPath + oldName + "/";

        if(optionalUser.isEmpty())
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при переименовании папки (пользователя не существует)"), HttpStatus.BAD_REQUEST);
        Optional<Folder> optionalFolder = folderRepository.findFolderByUserIdAndPath(optionalUser.get().getUser_id(), folderPath);
        if(optionalFolder.isEmpty())
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при переименовании папки (папки не существует)"), HttpStatus.BAD_REQUEST);
        folderRepository.renameInFolders(fullPath + newName + "/",
                newName,
                optionalFolder.get().getFolderId());
        return ResponseEntity.ok("Папка переименовалась корректно");
    }

    public ResponseEntity<?> deleteFolder(FolderDeleteRequest folderDeleteRequest) {
        List<MinioDto> files = null;
        try {
            files = fileService.getAllUserFiles(folderDeleteRequest.getUsername(), folderDeleteRequest.getFullPath());
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<DeleteObject> objects = convertToDeleteObjects(files);

        Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                .bucket(folderDeleteRequest.getUsername())
                .objects(objects)
                .build());

        results.forEach(deleteErrorResult -> {
            try {
                deleteErrorResult.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return ResponseEntity.ok("папка удалена");
    }

    public ResponseEntity<?> addFolderToFavorite(FolderFavoriteRequest folderFavoriteRequest) {
        Optional<User> optionalUser = userRepository.findByUsername(folderFavoriteRequest.getUsername());
        if(optionalUser.isEmpty()){
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при добавлении папки в избранное"), HttpStatus.BAD_REQUEST);
        }
        Optional<Folder> op = folderRepository.findFolderByUserIdAndPath(optionalUser.get().getUser_id(),folderFavoriteRequest.getFullPath());
        if (op.isPresent()) {
            Folder folder = op.get();
            FavoriteFolder favoriteFolder = new FavoriteFolder();
            favoriteFolder.setUserId(folder.getUserId());
            favoriteFolder.setFolderId(folder.getFolderId());
            favoriteFolderRepository.save(favoriteFolder);
            return ResponseEntity.ok("Папка добавлена в избранное");
        }
        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при добавлении папки в избранное"), HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<?> removeFolderFromFavorite(FolderFavoriteRequest folderFavoriteRequest) {
        Optional<User> optionalUser = userRepository.findByUsername(folderFavoriteRequest.getUsername());
        if(optionalUser.isEmpty()){
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при удалении папки из избранного"), HttpStatus.BAD_REQUEST);
        }
        Optional<Folder> op = folderRepository.findFolderByUserIdAndPath(optionalUser.get().getUser_id(),folderFavoriteRequest.getFullPath());
        if (op.isPresent()) {
            Folder folder = op.get();
            favoriteFolderRepository.deleteFromFavorite(folder.getUserId(), folder.getFolderId());
            return ResponseEntity.ok("Папка удалена из избранного");
        }
        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при удалении папки из избранного"), HttpStatus.BAD_REQUEST);
    }
}
