package com.monkeyteam.monkeycloud.controllers;

import com.monkeyteam.monkeycloud.dtos.PrivateAccessDto;
import com.monkeyteam.monkeycloud.services.PrivateAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class PrivateAccessController {

    private PrivateAccessService privateAccessService;

    @Autowired
    public void setPrivateAccessService(PrivateAccessService privateAccessService){
        this.privateAccessService = privateAccessService;
    }

    @PostMapping("/getPrivateAccess")
    public ResponseEntity<?> getPrivateAccess(@RequestBody PrivateAccessDto privateAccessDto){
        return privateAccessService.getPrivateAccess(privateAccessDto);
    }
}
