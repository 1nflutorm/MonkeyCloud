package com.monkeyteam.monkeycloud.controllers;

import com.monkeyteam.monkeycloud.dtos.ListOfData;
import com.monkeyteam.monkeycloud.dtos.folderDtos.OpenFolderRequest;
import com.monkeyteam.monkeycloud.repositories.FolderRepository;
import com.monkeyteam.monkeycloud.services.FolderService;
import com.monkeyteam.monkeycloud.services.PublicAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    @PutMapping("/openFolder")
    public ResponseEntity<?> openFolder(@RequestBody OpenFolderRequest openFolderRequest){
        return publicAccessService.openFolder(openFolderRequest);
    }
}
