package com.monkeyteam.monkeycloud.controllers;

import com.monkeyteam.monkeycloud.dtos.ListOfData;
import com.monkeyteam.monkeycloud.dtos.fileDtos.*;
import com.monkeyteam.monkeycloud.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class FileController {
    private FileService fileService;

    @Autowired
    public void setFileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/uploadFile")
    public ResponseEntity<?> uploadFile(@ModelAttribute FileUploadRequest file) {
        return fileService.uploadFile(file);
    }

    @GetMapping("/downloadFile")
    public ResponseEntity<?> downloadFile(@ModelAttribute FileDownloadRequest fileDownloadRequest) {
        ByteArrayResource byteArray = fileService.downloadFile(fileDownloadRequest);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + fileDownloadRequest.getFullPath())
                .body(byteArray);
    }

    @DeleteMapping("/deleteFile")
    public ResponseEntity<?> deleteFile(HttpServletRequest httpServletRequest) {
        String username = httpServletRequest.getParameter("username");
        String fullPath = httpServletRequest.getParameter("fullPath");
        return fileService.deleteFile(new FileDeleteRequest(username, fullPath));
    }

    @PutMapping("/renameFile")
    public ResponseEntity<?> renameFile(@RequestBody FileRenameRequest file) {
        return fileService.renameFile(file);
    }

    @GetMapping("/getFiles")
    public ResponseEntity<?> getFiles(HttpServletRequest httpServletRequest) {
        String username = httpServletRequest.getParameter("username");
        String folder = httpServletRequest.getParameter("folder");
        return new ResponseEntity<>(new ListOfData(fileService.getUserFiles(new GetFilesRequest(username, folder))), HttpStatus.OK);
    }

    @PostMapping("/addToFavorite")
    public ResponseEntity<?> addToFavorite(@RequestBody FileFavoriteRequest fileFavoriteRequest){
        return fileService.addToFavoriteFile(fileFavoriteRequest);
    }

    @PostMapping("/removeFromFavorite")
    public ResponseEntity<?> removeFromFavorite(@ModelAttribute FileFavoriteRequest fileFavoriteRequest){
        return fileService.removeFromFavorites(fileFavoriteRequest);
    }

}
