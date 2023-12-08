package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.dtos.MinioDto;
import com.monkeyteam.monkeycloud.dtos.SizeDto;
import com.monkeyteam.monkeycloud.dtos.fileDtos.*;
import com.monkeyteam.monkeycloud.entities.FavoriteFile;
import com.monkeyteam.monkeycloud.entities.Folder;
import com.monkeyteam.monkeycloud.entities.User;
import com.monkeyteam.monkeycloud.exeptions.AppError;
import com.monkeyteam.monkeycloud.repositories.FavoriteFileRepository;
import com.monkeyteam.monkeycloud.repositories.FolderRepository;
import com.monkeyteam.monkeycloud.repositories.UserRepository;
import com.monkeyteam.monkeycloud.utils.FileAndFolderUtil;


import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {
    private MinioClient minioClient;

    private final int KB = 1024;
    private final int MB = 1048576;
    private FileAndFolderUtil fileAndFolderUtil;
    private FolderRepository folderRepository;
    private FavoriteFileRepository favoriteFileRepository;
    private UserRepository userRepository;
    private MinioService minioService;

    @Autowired
    public void setMinioService(MinioService minioService) {
        this.minioService = minioService;
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
    public void setMinioClient(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Autowired
    public void setFavoriteFileRepository(FavoriteFileRepository favoriteFileRepository) {
        this.favoriteFileRepository = favoriteFileRepository;
    }

    @Autowired
    public void setFileAndFolderUtil(FileAndFolderUtil fileAndFolderUtil) {
        this.fileAndFolderUtil = fileAndFolderUtil;
    }

    private List<MinioDto> getUserFiles(String username, String folder, boolean isRecursive) throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(username)
                .prefix(folder)
                .recursive(isRecursive)
                .build());
        List<MinioDto> files = new ArrayList<>();

        results.forEach(result -> {
            try {
                Item item = result.get();

                Long size = item.size();//размер в байтах
                String postfix = "bytes";
                if (size >= KB && size < MB) {
                    size /= KB;
                    postfix = "kb";
                } else if (size >= MB) {
                    size /= MB;
                    postfix = "mb";
                }

                Boolean isFavorite = fileAndFolderUtil.checkFavorite(item, username);

                String[] newNames = fileAndFolderUtil.getCorrectNamesForItem(item, folder);

                String date = null;
                Boolean isDir = item.isDir();
                Long folderId = -1L;
                if (!isDir) {
                    date = parseDate(item.lastModified().toString());
                } else {
                    Optional<User> user = userRepository.findByUsername(username);
                    Optional<Folder> optionalFolder = folderRepository.findFolderByUserIdAndPath(user.get().getUser_id(), item.objectName());
                    folderId = optionalFolder.get().getFolderId();
                }

                MinioDto object = new MinioDto(
                        username,
                        folderId,
                        isDir,
                        item.objectName(),//Path
                        newNames[0],
                        newNames[1].equals("") ? username : username + "/" + newNames[1],//bread-crumbs
                        Long.toString(size) + " " + postfix,
                        isFavorite,
                        date);
                files.add(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return files;
    }

    public List<MinioDto> getUserFiles(GetFilesRequest getFilesRequest) {
        List<MinioDto> list = null;
        try {
            list = getUserFiles(getFilesRequest.getUsername(), getFilesRequest.getFolder(), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<MinioDto> getAllUserFiles(String username, String folder) throws Exception {
        return getUserFiles(username, folder, true);
    }

    public ResponseEntity<?> uploadFile(FileUploadRequest fileUploadRequest) {
        long size = fileUploadRequest.getMultipartFile().getSize() + minioService.getSizeOfBucket(fileUploadRequest.getUsername());
        if (size > MinioService.LIMIT_SIZE) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Превышен допустимый объём хранилища"), HttpStatus.BAD_REQUEST);
        }
        InputStream inputStream = null;
        try {
            inputStream = fileUploadRequest.getMultipartFile().getInputStream();
            minioClient.putObject(PutObjectArgs.builder()/////////////
                    .bucket(fileUploadRequest.getUsername())//путь передается без названия бакета и названия файла
                    //например folder/secFolder/
                    //при этом имя бакета 1nflutrom, а название передаваемого файла png.png
                    .object(fileUploadRequest.getFullPath() + fileUploadRequest.getMultipartFile().getOriginalFilename())
                    .stream(inputStream, fileUploadRequest.getMultipartFile().getSize(), -1)
                    .build());
        } catch (Exception e) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при загрузке файла"), HttpStatus.BAD_REQUEST);
        }
        try {
            inputStream.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return ResponseEntity.ok(new SizeDto(size, "Файл загружен корректно"));
    }

    public ByteArrayResource downloadFile(FileDownloadRequest fileDownloadRequest) {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(fileDownloadRequest.getUsername())
                .object(fileDownloadRequest.getFullPath())
                .build();
        ByteArrayResource byteArrayResource = null;
        try (GetObjectResponse object = minioClient.getObject(getObjectArgs)) {
            byteArrayResource = new ByteArrayResource(object.readAllBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byteArrayResource;
    }


    public ResponseEntity<?> deleteFile(FileDeleteRequest fileDeleteRequest) {
        try {
            minioClient.removeObject(RemoveObjectArgs
                    .builder()
                    .bucket(fileDeleteRequest.getUsername())
                    .object(fileDeleteRequest.getFullPath())//передается только путь, без названия бакета и без первого "/" пример: folder/secFolder/360fx360f.png
                    .build());
        } catch (Exception e) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при удалении файла"), HttpStatus.BAD_REQUEST);
        }
        long size = minioService.getSizeOfBucket(fileDeleteRequest.getUsername());
        return ResponseEntity.ok(new SizeDto(size, "Файл успешно удалён"));
    }

    public ResponseEntity<?> addToFavoriteFile(FileFavoriteRequest fileFavoriteRequest) {
        Optional<User> userOptional = userRepository.findByUsername(fileFavoriteRequest.getUsername());
        FavoriteFile favoriteFile = new FavoriteFile();
        if (userOptional.isEmpty()) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при нахождении пользователя"), HttpStatus.BAD_REQUEST);
        }
        User user = userOptional.get();
        String path = fileFavoriteRequest.getFullPath();
        int pathIndex = path.lastIndexOf('/');
        Optional<Folder> optional = folderRepository.findFolderByUserIdAndPath(user.getUser_id(), pathIndex == -1 ? "" : path.substring(0, pathIndex) + "/");
        if (optional.isPresent()) {
            Folder folder = optional.get();
            favoriteFile.setFilePath(path);
            favoriteFile.setUserId(folder.getUserId());
            favoriteFile.setFolderId(folder.getFolderId());
            favoriteFileRepository.save(favoriteFile);
            return ResponseEntity.ok("Файл успешно добавлен в избранное");
        }
        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при добавлении файла в избранное"), HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<?> removeFromFavorites(FileFavoriteRequest fileFavoriteRequest) {
        User user = userRepository.findByUsername(fileFavoriteRequest.getUsername()).get();
        favoriteFileRepository.deleteFromFavorite(user.getUser_id(), fileFavoriteRequest.getFullPath());
        return ResponseEntity.ok("Файл успешно удалён из избранного");
    }

    public ResponseEntity<?> renameFile(FileRenameRequest fileRenameRequest) {
        try {
            Optional<User> optionalUser = userRepository.findByUsername(fileRenameRequest.getUsername());
            if (optionalUser.isEmpty())
                return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при переименовании файла"), HttpStatus.BAD_REQUEST);

            Long userId = optionalUser.get().getUser_id();
            String newPath = fileRenameRequest.getFullPath() + fileRenameRequest.getNewName();
            String oldPath = fileRenameRequest.getFullPath() + fileRenameRequest.getOldName();
            minioClient.copyObject(CopyObjectArgs
                    .builder()
                    .bucket(fileRenameRequest.getUsername())
                    .object(fileRenameRequest.getFullPath() + fileRenameRequest.getNewName())
                    .source(CopySource
                            .builder()
                            .bucket(fileRenameRequest.getUsername())
                            .object(fileRenameRequest.getFullPath() + fileRenameRequest.getOldName()) //путь передается без названия бакета и названия файла
                            //например folder/secFolder/
                            //при этом имя бакета 1nflutrom, а название файла png.png
                            .build())
                    .build());
            deleteFile(new FileDeleteRequest(fileRenameRequest.getUsername(), fileRenameRequest.getFullPath() + "/" + fileRenameRequest.getOldName()));
            Optional<Folder> optionalFolder = folderRepository.findFolderByUserIdAndPath(userId, fileRenameRequest.getFullPath());
            if (optionalFolder.isPresent()) {
                Long folderId = optionalFolder.get().getFolderId();
                favoriteFileRepository.renameInFavoriteFiles(newPath, userId, folderId, oldPath);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при переименовании файла"), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("Файл переименован успешно");
    }

    public static String parseDate(String date){
        String newDate = null;
        newDate = date;
        int index = newDate.indexOf('T');
        newDate = newDate.substring(0, index);
        String[] ymd = newDate.split("-");//year, month, day
        return ymd[2] + "." + ymd[1] + "." + ymd[0];
    }
}
