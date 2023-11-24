package com.monkeyteam.monkeycloud.controllers;

import com.monkeyteam.monkeycloud.dtos.ListOfData;
import com.monkeyteam.monkeycloud.dtos.fileDtos.*;

import com.monkeyteam.monkeycloud.repositories.FavoriteFileReposiory;
import com.monkeyteam.monkeycloud.exeptions.AppError;
import com.monkeyteam.monkeycloud.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/uploadFile")
    public ResponseEntity<?> uploadFile(@ModelAttribute FileUploadRequest file) {
        return fileService.uploadFile(file);
    }

    @GetMapping("/downloadFile")
    public ResponseEntity<?> downloadFile(@ModelAttribute FileDownloadRequest file) {
        return fileService.downloadFile(file);
    }

    @DeleteMapping("/deleteFile")
    public ResponseEntity<?> deleteFile(@ModelAttribute FileDeleteRequest file) {
        return fileService.deleteFile(file);
    }

    @PutMapping("/renameFile")
    public ResponseEntity<?> renameFile(@ModelAttribute FileRenameRequest file) {
        return fileService.renameFile(file);
    }

    @GetMapping("/getFiles")
    public ResponseEntity<?> getFiles(@ModelAttribute GetFilesRequest filesRequest) {
        return new ResponseEntity<>(new ListOfData(fileService.getUserFiles(filesRequest)), HttpStatus.OK);
    }

    @PostMapping("/addToFavorite")
    public ResponseEntity<?> addToFavorite(@ModelAttribute FileFavoriteRequest fileFavoriteRequest){
        return fileService.addToFavoriteFile(fileFavoriteRequest);
    }

    @PostMapping("/removeFromFavorite")
    public ResponseEntity<?> removeFromFavorite(@ModelAttribute FileFavoriteRequest fileFavoriteRequest){
        return fileService.removeFromFavorites(fileFavoriteRequest);
    }

}
