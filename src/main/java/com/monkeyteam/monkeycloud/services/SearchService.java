
package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.dtos.ListOfData;
import com.monkeyteam.monkeycloud.dtos.MinioDto;
import com.monkeyteam.monkeycloud.exeptions.AppError;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {
    private FileService fileService;

    @Autowired
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    private List<MinioDto> getObjectList(String username){
        List<MinioDto> objectList = null;
        try {
            objectList = fileService.getAllUserFiles(username, "");
        } catch (Exception e) {
            e.printStackTrace();
            //ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "ошибка поиска файла"), HttpStatus.BAD_REQUEST);
        }
        return objectList;
    }

    public ResponseEntity<?> privateSearchByFileName(String username, String filename) {
        List<MinioDto> objectList = getObjectList(username);
        List<MinioDto> foundFiles = new ArrayList<MinioDto>();
        objectList.forEach(minioObject -> {
            if (minioObject.getName().startsWith(filename) && !minioObject.getIsDir())
                foundFiles.add(minioObject);
        });
        return ResponseEntity.ok(new ListOfData(foundFiles));
    }

    public ResponseEntity<?> privateSearchByDate(String username, String date){
        List<MinioDto> objectList = getObjectList(username);
        List<MinioDto> foundFiles = new ArrayList<MinioDto>();
        objectList.forEach(minioObject -> {
            if (minioObject.getDate().contains(date))
                foundFiles.add(minioObject);
        });
        return ResponseEntity.ok(new ListOfData(foundFiles));
    }
}
