package com.monkeyteam.monkeycloud.controllers;

import com.monkeyteam.monkeycloud.dtos.PrivateAccessDto;
import com.monkeyteam.monkeycloud.services.PrivateAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("getPrivateAccess")
    public void getPrivateAccess(@RequestBody PrivateAccessDto privateAccessDto){

    }
}
