package com.monkeyteam.monkeycloud.controllers;

import com.monkeyteam.monkeycloud.services.AdminService;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminController {

    private AdminService adminService;

    @Autowired
    public void setAdminService(AdminService adminService){
        this.adminService = adminService;
    }
    @GetMapping("/getFilesByAdmin")
    public ResponseEntity<?> getFilesByAdmin(){
            return adminService.getFilesByAdmin();
    }

}
