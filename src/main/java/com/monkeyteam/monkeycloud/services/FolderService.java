package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.dtos.folderDtos.FolderDeleteRequest;
import com.monkeyteam.monkeycloud.dtos.folderDtos.FolderRenameRequest;
import com.monkeyteam.monkeycloud.dtos.folderDtos.FolderUploadRequest;
import com.monkeyteam.monkeycloud.dtos.MinioDto;
import com.monkeyteam.monkeycloud.exeptions.AppError;
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

    @Autowired
    public void setMinioClient(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Autowired
    public void setFileService (FileService fileService){
        this.fileService = fileService;
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
            List<SnowballObject> snowballObject = convertToSnowballObjects(folderUploadRequest);
            minioClient.uploadSnowballObjects(UploadSnowballObjectsArgs
                    .builder()
                    .bucket(folderUploadRequest.getUsername())
                    .objects(snowballObject)
                    .build());

        } catch (Exception e) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при загрузке папки"), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("Папка загрузилась корректно");
    }

    public ResponseEntity<?> renameFolder(FolderRenameRequest folderRenameRequest) {
        List<MinioDto> files = null;
        try {
            files = fileService.getAllUserFiles(folderRenameRequest.getUsername(), folderRenameRequest.getFullPath() + folderRenameRequest.getOldName());
        } catch (Exception e) {
            return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при переименовании папки"), HttpStatus.BAD_REQUEST);
        }
        for (MinioDto file : files) {
            try {
                int start = (folderRenameRequest.getFullPath() + folderRenameRequest.getOldName()).length();
                String path = file.getPath();
                path = path.substring(start);
                path = folderRenameRequest.getFullPath() + folderRenameRequest.getNewName() + path;

                String first = folderRenameRequest.getFullPath() +  folderRenameRequest.getNewName() + "/" + file.getPath();
                String sec = file.getPath();
                minioClient.copyObject(CopyObjectArgs
                        .builder()
                        .bucket(folderRenameRequest.getUsername())
                        .object(path)
                        .source(CopySource.builder()
                                .bucket(folderRenameRequest.getUsername())
                                .object(file.getPath())
                                .build())
                        .build());
            } catch (Exception e) {
                return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Ошибка при переименовании папки"), HttpStatus.BAD_REQUEST);
            }
        }
        FolderDeleteRequest folderDeleteRequest = new FolderDeleteRequest(folderRenameRequest.getUsername(), folderRenameRequest.getFullPath() + folderRenameRequest.getOldName());
        deleteFolder(folderDeleteRequest);
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
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        return ResponseEntity.ok("папка удалена");
    }
}
