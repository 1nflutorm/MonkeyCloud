package com.monkeyteam.monkeycloud.controllers;

import com.monkeyteam.monkeycloud.dtos.GrantAccessDto;
import com.monkeyteam.monkeycloud.dtos.PrivateAccessDto;
import com.monkeyteam.monkeycloud.services.PrivateAccessService;
import com.monkeyteam.monkeycloud.services.PublicAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PrivateAccessController {

    private PrivateAccessService privateAccessService;

    private PublicAccessService publicAccessService;

    @Autowired
    public void setPublicAccessService(PublicAccessService publicAccessService){
        this.publicAccessService = publicAccessService;
    }
    @Autowired
    public void setPrivateAccessService(PrivateAccessService privateAccessService){
        this.privateAccessService = privateAccessService;
    }

    @GetMapping("/getPrivateFolder")
    public ResponseEntity<?> getFilesInPrivateFolder(@RequestParam("owner") String owner,
                                                     @RequestParam("customer") String customer,
                                                     @RequestParam("folderId") String folderId){
        return privateAccessService.getFilesInPrivateFolder(owner, customer, Long.parseLong(folderId));
    }

    @PostMapping("/getPrivateAccess")
    public ResponseEntity<?> getPrivateAccess(@RequestBody PrivateAccessDto privateAccessDto){
        return privateAccessService.getPrivateAccess(privateAccessDto);
    }

    @GetMapping("/getFilesInPrivateFolder")
    public ResponseEntity<?> getFilesInPublicFolder(@RequestParam("folderId") Long folderId){
        return publicAccessService.getFilesInPublicFolder(folderId, 2);
    }

    @PostMapping("/grant-access")
    public ResponseEntity<?> grantAccess(@RequestBody GrantAccessDto grantAccessDto){
        return privateAccessService.grantAccess(grantAccessDto);
    }

}
