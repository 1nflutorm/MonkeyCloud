package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.dtos.fileDtos.*;
import com.monkeyteam.monkeycloud.dtos.MinioDto;
import com.monkeyteam.monkeycloud.entities.FavoriteFile;
import com.monkeyteam.monkeycloud.entities.Folder;
import com.monkeyteam.monkeycloud.exeptions.AppError;
import com.monkeyteam.monkeycloud.repositories.FavoriteFileReposiory;
import com.monkeyteam.monkeycloud.repositories.FolderRepository;
import io.minio.*;
import io.minio.messages.Item;
import org.apache.tomcat.jni.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.replaceAll;

@Service
public class FileService {
    private MinioClient minioClient;
    private FavoriteFileReposiory favoriteFileReposiory;
    private FolderRepository folderRepository;

    @Autowired
    public void setMinioClient(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Autowired
    public void setFavoriteFileReposiory(FavoriteFileReposiory favoriteFileReposiory) {
        this.favoriteFileReposiory = favoriteFileReposiory;
    }

    @Autowired
    public void setFolderRepository(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
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
                String[] newNames = getCorrectNamesForItem(item, folder);
                MinioDto object = new MinioDto(
                        username,
                        item.isDir(),
                        newNames[1].equals("") ? username : username + "/" + newNames[1],
                        newNames[0]);
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
        InputStream inputStream = null;
        try {
            inputStream = fileUploadRequest.getMultipartFile().getInputStream();
            minioClient.putObject(PutObjectArgs.builder()
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
        return ResponseEntity.ok("Файл загружен корректно");
    }

    public ResponseEntity<?> downloadFile(FileDownloadRequest fileDownloadRequest) {
        InputStream inputStream = null;
        try {
            inputStream = minioClient.getObject(GetObjectArgs
                    .builder()
                    .bucket(fileDownloadRequest.getUsername())
                    .object(fileDownloadRequest.getFullPath())
                    .build());
        } catch (Exception e) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при скачивании файла"), HttpStatus.BAD_REQUEST);
        }
        try {
            inputStream.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return ResponseEntity.ok("Файл скачен успешно");
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
        return ResponseEntity.ok("Файл удалён успешно");
    }

    @Transactional
    public ResponseEntity<?> renameFile(FileRenameRequest fileRenameRequest) {
        try {
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
        } catch (Exception e) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при переименовании файла"), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("Файл переименован успешно");
    }

    private String[] getCorrectNamesForItem(Item item, String folder) {
        String objectName = "";
        int lastSlash = item.objectName().lastIndexOf('/');
        if (!item.isDir()) {//если не дитректория, то удаляем последний слэш
            objectName = item.objectName().substring(lastSlash + 1);
        } else {// если директория, то удаляем предпоследний и послдежний слэш
            objectName = item.objectName().substring(0, lastSlash);
            objectName = objectName.substring(objectName.lastIndexOf('/') + 1);
        }
        lastSlash = folder.lastIndexOf('/');
        String folderName = "";
        if (lastSlash == folder.length() - 1 && lastSlash != -1)
            folderName = folder.substring(0, lastSlash);
        return new String[]{objectName, folderName};
    }

    public FavoriteFile addToFavoriteFile(FileFavoriteRequest fileFavoriteRequest) {
        FavoriteFile favoriteFile = new FavoriteFile();
        String path = fileFavoriteRequest.getFullPath();
        path = path.replaceAll(fileFavoriteRequest.getFileName(), "");
        if (folderRepository.getFolder(path).isPresent()) {
            Folder folder = folderRepository.getFolder(path).get();
            favoriteFile.setFilePath(path);
            favoriteFile.setUserId(folder.getUserId());
            favoriteFile.setFolderId(folder.getFolderId());
            return favoriteFileReposiory.save(favoriteFile);
        }
        return favoriteFile;
    }
}
