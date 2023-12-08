package com.monkeyteam.monkeycloud.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PublicAccessController {

    @GetMapping("/publicAccess")
    public ResponseEntity<?> getFoldersFromPublicAccess(){
        return null;
    }
}
