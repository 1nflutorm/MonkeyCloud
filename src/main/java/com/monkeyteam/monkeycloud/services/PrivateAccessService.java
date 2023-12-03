package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.dtos.PrivateAccessDto;
import com.monkeyteam.monkeycloud.entities.Folder;
import com.monkeyteam.monkeycloud.repositories.FolderRepository;
import lombok.RequiredArgsConstructor;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrivateAccessService {

    private FolderRepository folderRepository;

    @Autowired
    public void setFolderRepository (FolderRepository folderRepository){
        this.folderRepository = folderRepository;
    }

    public ResponseEntity<?> getPrivateAccess(PrivateAccessDto privateAccessDto){
        /*
        * TODO:
        *  1. найти tg_id пользователя в БД
        *   2. отправить get запрос боту
        *  */
        return null;
    }

    public void sendToBot(){

    }

}
