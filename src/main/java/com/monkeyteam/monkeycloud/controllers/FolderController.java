package com.monkeyteam.monkeycloud.controllers;

import com.monkeyteam.monkeycloud.dtos.folderDtos.FolderDeleteRequest;
import com.monkeyteam.monkeycloud.dtos.folderDtos.FolderRenameRequest;
import com.monkeyteam.monkeycloud.dtos.folderDtos.FolderUploadRequest;
import com.monkeyteam.monkeycloud.services.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class FolderController {
    private final FolderService folderService;
    @PostMapping("/uploadFolder")
    public ResponseEntity<?> uploadFile(@ModelAttribute FolderUploadRequest file) {
        return folderService.uploadFolder(file);
    }

    @DeleteMapping("/deleteFolder")
    public ResponseEntity<?> deleteFile(HttpServletRequest httpServletRequest) {
        String username = httpServletRequest.getParameter("username");
        String fullPath = httpServletRequest.getParameter("fullPath");
        return folderService.deleteFolder(new FolderDeleteRequest(username, fullPath));
    }

    @PutMapping("/renameFolder")
    public ResponseEntity<?> renameFile(@RequestBody FolderRenameRequest file) {
        return folderService.renameFolder(file);
    }

}
