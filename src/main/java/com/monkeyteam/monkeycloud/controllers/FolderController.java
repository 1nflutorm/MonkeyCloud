package com.monkeyteam.monkeycloud.controllers;

import com.monkeyteam.monkeycloud.dtos.folderDtos.FolderDeleteRequest;
import com.monkeyteam.monkeycloud.dtos.folderDtos.FolderFavoriteRequest;
import com.monkeyteam.monkeycloud.dtos.folderDtos.FolderRenameRequest;
import com.monkeyteam.monkeycloud.dtos.folderDtos.FolderUploadRequest;
import com.monkeyteam.monkeycloud.services.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequiredArgsConstructor
public class FolderController {
    private final FolderService folderService;

    @RequestMapping(path = "/uploadFolder", method = POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> uploadFile(@RequestParam("username") String username,
                                        @RequestParam("fullPath") String fullPath,
                                        @RequestPart("multipartFile") List<MultipartFile> multipartFiles) {
        return folderService.uploadFolder(new FolderUploadRequest(multipartFiles, fullPath, username));
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

    @PostMapping("/addFolderToFavorite")
    public ResponseEntity<?> addFolderToFavorite(@RequestBody FolderFavoriteRequest folderFavoriteRequest){
        return folderService.addFolderToFavorite(folderFavoriteRequest);
    }

    @PostMapping("/removeFolderFromFavorite")
    public ResponseEntity<?> removeFolderFromFavorite(@RequestBody FolderFavoriteRequest folderFavoriteRequest){
        return folderService.removeFolderFromFavorite(folderFavoriteRequest);
    }
}
