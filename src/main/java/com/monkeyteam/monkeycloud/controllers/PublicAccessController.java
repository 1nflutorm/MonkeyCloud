package com.monkeyteam.monkeycloud.controllers;

import com.monkeyteam.monkeycloud.services.PublicAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PublicAccessController {

    private PublicAccessService publicAccessService;

    @Autowired
    public void setFolderRepository(PublicAccessService publicAccessService){
        this.publicAccessService = publicAccessService;
    }
    @GetMapping("/publicAccess")
    public ResponseEntity<?> getFoldersFromPublicAccess(){
        return new ResponseEntity<>(publicAccessService.getPublicFolders(), HttpStatus.OK);
    }

    @GetMapping("/getFilesInPublicFolder")
    public ResponseEntity<?> getFilesInPublicFolder(@RequestParam("folderId") Long folderId){
         return publicAccessService.getFilesInPublicFolder(folderId);
    }

    @PutMapping("/openFolder")
    public ResponseEntity<?> openFolder(@RequestBody Long folderId){
        return publicAccessService.openFolder(folderId);
    }
}
