package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.repositories.dtos.folderDtos.FolderDeleteRequest;
import com.monkeyteam.monkeycloud.repositories.dtos.folderDtos.FolderRenameRequest;
import com.monkeyteam.monkeycloud.repositories.dtos.folderDtos.FolderUploadRequest;
import com.monkeyteam.monkeycloud.repositories.dtos.MinioDto;
import com.monkeyteam.monkeycloud.exeptions.AppError;
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

@Service
@RequiredArgsConstructor
public class FolderService {
    private FileService fileService;
    private MinioClient minioClient;

    private FileAndFolderUtil fileAndFolderUtil;

    @Autowired
    public void setFileAndFolderUtil(FileAndFolderUtil fileAndFolderUtil){
        this.fileAndFolderUtil = fileAndFolderUtil;
    }

    @Autowired
    public void setFileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Autowired
    public void setMinioClient(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    private List<DeleteObject> convertToDeleteObjects(List<MinioDto> files) {
        List<DeleteObject> objects = new ArrayList<>();
        for (MinioDto file : files) {
            objects.add(new DeleteObject(file.getUsername() + file.getPath()));
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
                    folderUploadRequest.getUsername() + file.getOriginalFilename(),
                    file.getInputStream(),
                    file.getSize(),
                    null);
            objects.add(snowballObject);
        }
        return objects;
    }

    public ResponseEntity<?> uploadFolder(FolderUploadRequest folderUploadRequest) {
        try {
            List<SnowballObject> snowballObjects = convertToSnowballObjects(folderUploadRequest);
            minioClient.uploadSnowballObjects(UploadSnowballObjectsArgs
                    .builder()
                    .bucket(folderUploadRequest.getUsername())
                    .objects(snowballObjects)
                    .build());

            fileAndFolderUtil.addDirsToDataBaseFromUploadedFolder(snowballObjects, folderUploadRequest.getUsername());///!!!НЕ ТЕСТИРОВАЛОСЬ
        } catch (Exception e) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при загрузке папки"), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("Папка загрузилась корректно");
    }

    public ResponseEntity<?> renameFolder(FolderRenameRequest folderRenameRequest) {
        List<MinioDto> files = null;
        try {
            files = fileService.getAllUserFiles(folderRenameRequest.getUsername(), folderRenameRequest.getFullPath());
        } catch (Exception e) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при переименовании папки"), HttpStatus.BAD_REQUEST);
        }
        for (MinioDto file : files) {
            try {
                minioClient.copyObject(CopyObjectArgs
                        .builder()
                        .bucket(folderRenameRequest.getUsername())
                        .object(folderRenameRequest.getFullPath() + folderRenameRequest.getNewName() + "/")
                        .source(CopySource.builder()
                                .bucket(folderRenameRequest.getUsername())
                                .object(folderRenameRequest.getFullPath() + folderRenameRequest.getOldName() + "/")
                                .build())
                        .build());

                FolderDeleteRequest folderDeleteRequest = new FolderDeleteRequest(folderRenameRequest.getUsername(), folderRenameRequest.getFullPath());
                deleteFolder(folderDeleteRequest);
            } catch (Exception e) {
                return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при переименовании папки"), HttpStatus.BAD_REQUEST);
            }
        }
        return ResponseEntity.ok("Папка переименовалась корректно");
    }

    public void deleteFolder(FolderDeleteRequest folderDeleteRequest) {
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
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
