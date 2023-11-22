package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.dtos.FileDownloadRequest;
import com.monkeyteam.monkeycloud.dtos.FileRenameRequest;
import com.monkeyteam.monkeycloud.dtos.FileUploadRequest;
import com.monkeyteam.monkeycloud.dtos.MinioDto;
import com.monkeyteam.monkeycloud.exeptions.AppError;
import io.minio.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {
    private MinioClient minioClient;
    @Autowired
    public void setMinioClient(MinioClient minioClient) {
        this.minioClient = minioClient;
    }








    private List<MinioDto> getUserFiles(String username, String folder, boolean isRecursive) throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(username)
                .prefix(username + folder)
                .recursive(isRecursive)
                .build());
        List<MinioDto> files = new ArrayList<>();

        results.forEach(result -> {
            try {
                Item item = result.get();
                MinioDto object = new MinioDto(
                        username,
                        item.isDir(),
                        username + folder,
                        folder);
                files.add(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return files;
    }
    public List<MinioDto> getUserFiles(String username, String folder) throws Exception {
        return getUserFiles(username, folder, false);
    }
    public List<MinioDto> getAllUserFiles(String username, String folder) throws Exception {
        return getUserFiles(username, folder, true);
    }








    public ResponseEntity<?> uploadFile(FileUploadRequest fileUploadRequest) {
        InputStream inputStream = null;
        try {
            inputStream = fileUploadRequest.getMultipartFile().getInputStream();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(fileUploadRequest.getUsername())
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

    public ResponseEntity<?> deleteFile(FileDownloadRequest fileDeleteRequest) {
        try {
            minioClient.removeObject(RemoveObjectArgs
                    .builder()
                    .bucket(fileDeleteRequest.getUsername())
                    .object(fileDeleteRequest.getFullPath())
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
                            .object(fileRenameRequest.getFullPath() + fileRenameRequest.getOldName())
                            .build())
                    .build());
            deleteFile(new FileDownloadRequest(fileRenameRequest.getUsername(), fileRenameRequest.getFullPath() + "/" + fileRenameRequest.getOldName()));
        } catch (Exception e) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при переименовании файла"), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("Файл переименован успешно");
    }
}
