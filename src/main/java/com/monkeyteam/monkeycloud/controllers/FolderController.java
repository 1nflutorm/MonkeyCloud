package com.monkeyteam.monkeycloud.controllers;

import com.monkeyteam.monkeycloud.dtos.folderDtos.FolderDeleteRequest;
import com.monkeyteam.monkeycloud.dtos.folderDtos.FolderRenameRequest;
import com.monkeyteam.monkeycloud.dtos.folderDtos.FolderUploadRequest;
import com.monkeyteam.monkeycloud.services.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class FolderController {
    private final FolderService folderService;
    @PostMapping("/uploadFolder")
    public ResponseEntity<?> uploadFile(@ModelAttribute FolderUploadRequest file) {
        return folderService.uploadFolder(file);
    }

    @DeleteMapping("/deleteFolder")
    public void deleteFile(@ModelAttribute FolderDeleteRequest file) {
        folderService.deleteFolder(file);
    }

    @PutMapping("/renameFolder")
    public ResponseEntity<?> renameFile(@ModelAttribute FolderRenameRequest file) {
        return folderService.renameFolder(file);
    }

}
