package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.dtos.SizeDto;
import com.monkeyteam.monkeycloud.dtos.fileDtos.GetFilesRequest;
import com.monkeyteam.monkeycloud.dtos.folderDtos.*;
import com.monkeyteam.monkeycloud.dtos.MinioDto;
import com.monkeyteam.monkeycloud.entities.FavoriteFolder;
import com.monkeyteam.monkeycloud.entities.Folder;
import com.monkeyteam.monkeycloud.entities.User;
import com.monkeyteam.monkeycloud.exeptions.AppError;
import com.monkeyteam.monkeycloud.repositories.FavoriteFolderRepository;
import com.monkeyteam.monkeycloud.repositories.FolderRepository;
import com.monkeyteam.monkeycloud.repositories.InheritorFoldersRepository;
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
import java.sql.SQLException;
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

    private InheritorFoldersRepository inheritorFoldersRepository;
    private UserRepository userRepository;
    private MinioService minioService;

    @Autowired
    public void setInheritorFoldersRepository(InheritorFoldersRepository inheritorFoldersRepository){
        this.inheritorFoldersRepository = inheritorFoldersRepository;
    }

    @Autowired
    public void setMinioService(MinioService minioService) {
        this.minioService = minioService;
    }

    @Autowired
    public void setFolderRepository(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    @Autowired
    public void setFavoriteFolderRepository(FavoriteFolderRepository favoriteFolderRepository) {
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
    public void setUserRepository(UserRepository userRepository) {
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
        long size = minioService.getSizeOfBucket(folderUploadRequest.getUsername());
        for (MultipartFile multipartFile : folderUploadRequest.getFiles()) {
            size += multipartFile.getSize();
        }
        if (size > MinioService.LIMIT_SIZE) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Превышен допустимый объём хранилища"), HttpStatus.BAD_REQUEST);
        }
        try {
            List<SnowballObject> snowballObject = convertToSnowballObjects(folderUploadRequest);
            ///
            minioClient.uploadSnowballObjects(UploadSnowballObjectsArgs
                    .builder()
                    .bucket(folderUploadRequest.getUsername())
                    .objects(snowballObject)
                    .build());
            fileAndFolderUtil.addDirsToDataBaseFromUploadedFolder(snowballObject, folderUploadRequest.getUsername());
            ///
        } catch (Exception e) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при загрузке папки"), HttpStatus.BAD_REQUEST);
        }
//        String parentFolderName = folderUploadRequest.getFullPath();
//        Optional<Folder> parentFolder = folderRepository.findByFolderName(parentFolderName);
//        List<Long> childrenList = inheritorFoldersRepository.findChildren(parentFolder.get().getFolderId());
//        String fileName = folderUploadRequest.getFiles().get(0).getName();
//        for(Long childId : childrenList){
//            Optional<Folder> folder = folderRepository.findFolderByFolderId(childId);
//            if(fileName.startsWith(folder.get().getFolderName())){
//                folderRepository.setFolderAccess(parentFolder.get().getFolderAccess(), folder.get().getFolderId());
//            }
//        }
        return ResponseEntity.ok(new SizeDto(size/FileService.MB, "Папка загрузилась корректно"));
    }

    public ResponseEntity<?> renameFolder(FolderRenameRequest folderRenameRequest) {
        List<MinioDto> files = null;
        String username = folderRenameRequest.getUsername();
        String fullPath = folderRenameRequest.getFullPath();
        String oldName = folderRenameRequest.getOldName();
        String newName = folderRenameRequest.getNewName();
        String folderPath = fullPath + oldName + "/";
        try {
            files = fileService.getAllUserFiles(new GetFilesRequest(username, folderPath));
        } catch (Exception e) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при переименовании папки"), HttpStatus.BAD_REQUEST);
        }
        for (MinioDto file : files) {
            try {
                int start = (fullPath + oldName).length();
                String path = file.getPath();
                path = path.substring(start);
                path = fullPath + newName + path;

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
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty())
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при переименовании папки (пользователя не существует)"), HttpStatus.BAD_REQUEST);
        Long userId = optionalUser.get().getUser_id();
        Optional<Folder> optionalFolder = folderRepository.findFolderByUserIdAndPath(userId, folderPath);
        if (optionalFolder.isEmpty())
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при переименовании папки (папки не существует)"), HttpStatus.BAD_REQUEST);
        updateInnerFolders(userId, folderPath, fullPath + newName + "/");
        FolderDeleteRequest folderDeleteRequest = new FolderDeleteRequest(username, folderPath);
        deleteFolder(folderDeleteRequest);
        return ResponseEntity.ok("Папка переименовалась корректно");
    }

    private void updateInnerFolders(Long userId, String oldName, String newName) {
        List<Folder> folderList = folderRepository.getAll();
        for (Folder folder : folderList) {
            if (!folder.getFolderPath().startsWith(oldName)) {
                continue;
            }
            String fullName = folder.getFolderPath();
            Optional<Folder> folderToRename = folderRepository.findFolderByUserIdAndPath(userId, fullName);
            fullName = fullName.replaceFirst(oldName, newName);
            String folderName = newName.substring(0, newName.lastIndexOf('/'));
            folderName = folderName.substring(folderName.lastIndexOf('/') + 1);
            folderRepository.renameFolder(fullName, folderName, folderToRename.get().getFolderId());//ошибка
        }
    }

    public ResponseEntity<?> deleteFolder(FolderDeleteRequest folderDeleteRequest) {
        List<MinioDto> files = null;
        try {
            files = fileService.getAllUserFiles(new GetFilesRequest(folderDeleteRequest.getUsername(), folderDeleteRequest.getFullPath()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Optional<User> optionalUser = userRepository.findByUsername(folderDeleteRequest.getUsername());

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
        Optional<Folder> optionalFolder = folderRepository.findFolderByUserIdAndPath(optionalUser.get().getUser_id(), folderDeleteRequest.getFullPath());
        if (optionalFolder.isPresent()) {
            folderRepository.deleteFolderById(optionalFolder.get().getFolderId());
        }
        long size = minioService.getSizeOfBucket(folderDeleteRequest.getUsername());
        return ResponseEntity.ok(new SizeDto(size/FileService.MB, "Папка успешно удалена"));
    }

    public ResponseEntity<?> addFolderToFavorite(FolderFavoriteRequest folderFavoriteRequest) {
        Optional<User> optionalUser = userRepository.findByUsername(folderFavoriteRequest.getUsername());
        if (optionalUser.isEmpty()) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при добавлении папки в избранное"), HttpStatus.BAD_REQUEST);
        }
        Optional<Folder> op = folderRepository.findFolderByUserIdAndPath(optionalUser.get().getUser_id(), folderFavoriteRequest.getFullPath());
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
        if (optionalUser.isEmpty()) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при удалении папки из избранного"), HttpStatus.BAD_REQUEST);
        }
        Optional<Folder> op = folderRepository.findFolderByUserIdAndPath(optionalUser.get().getUser_id(), folderFavoriteRequest.getFullPath());
        if (op.isPresent()) {
            Folder folder = op.get();
            favoriteFolderRepository.deleteFromFavorite(folder.getUserId(), folder.getFolderId());
            return ResponseEntity.ok("Папка удалена из избранного");
        }
        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при удалении папки из избранного"), HttpStatus.BAD_REQUEST);
    }
}
