package com.monkeyteam.monkeycloud.controllers;

import com.monkeyteam.monkeycloud.dtos.FileDownloadRequest;
import com.monkeyteam.monkeycloud.dtos.FileRenameRequest;
import com.monkeyteam.monkeycloud.dtos.FileUploadRequest;
import com.monkeyteam.monkeycloud.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
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
    public ResponseEntity<?> deleteFile(@ModelAttribute FileDownloadRequest file) {
        return fileService.deleteFile(file);
    }

    @PutMapping("/renameFile")
    public ResponseEntity<?> renameFile(@ModelAttribute FileRenameRequest file) {
        return fileService.renameFile(file);
    }
}
