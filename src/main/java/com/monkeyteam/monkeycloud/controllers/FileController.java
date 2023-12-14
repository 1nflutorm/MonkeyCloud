package com.monkeyteam.monkeycloud.controllers;

import com.monkeyteam.monkeycloud.dtos.ListOfData;
import com.monkeyteam.monkeycloud.dtos.fileDtos.*;
import com.monkeyteam.monkeycloud.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequiredArgsConstructor
public class FileController {
    private FileService fileService;

    @Autowired
    public void setFileController(FileService fileService) {
        this.fileService = fileService;
    }

    @RequestMapping(path = "/uploadFile", method = POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> uploadFile(@RequestParam("username") String username,
                                        @RequestParam("fullPath") String fullPath,
                                        @RequestPart("multipartFile") MultipartFile multipartFile) {
        return fileService.uploadFile(new FileUploadRequest(username, fullPath, multipartFile));
    }

    @GetMapping(value = "/downloadFile", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> downloadFile(@RequestParam("username") String username,
                                          @RequestParam("fullPath") String fullPath) {

        ByteArrayResource byteArray = fileService.downloadFile(new FileDownloadRequest(username, fullPath));

        int index = fullPath.lastIndexOf('/');
        if(index != -1)
            fullPath = fullPath.substring(index);

        index = fullPath.lastIndexOf("/");
        String filename = index == -1 ? fullPath : fullPath.substring(index);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentDisposition(ContentDisposition.inline()
                .filename(filename, StandardCharsets.UTF_8)
                .build());

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(byteArray);
    }

    @DeleteMapping("/deleteFile")
    public ResponseEntity<?> deleteFile(HttpServletRequest httpServletRequest) {
        String username = httpServletRequest.getParameter("username");
        String fullPath = httpServletRequest.getParameter("fullPath");
        return fileService.deleteFile(new FileDeleteRequest(username, fullPath));
    }

    @PutMapping("/renameFile")
    public ResponseEntity<?> renameFile(@RequestBody FileRenameRequest file) {
        return fileService.renameFile(file);
    }

    @GetMapping("/getFiles")
    public ResponseEntity<?> getFiles(HttpServletRequest httpServletRequest) {
        String username = httpServletRequest.getParameter("username");
        String folder = httpServletRequest.getParameter("folder");
        return new ResponseEntity<>(new ListOfData(fileService.getUserFiles(new GetFilesRequest(username, folder))), HttpStatus.OK);
    }

    @PostMapping("/addToFavorite")
    public ResponseEntity<?> addToFavorite(@RequestBody FileFavoriteRequest fileFavoriteRequest){
        return fileService.addToFavoriteFile(fileFavoriteRequest);
    }

    @PostMapping("/removeFromFavorite")
    public ResponseEntity<?> removeFromFavorite(@RequestBody FileFavoriteRequest fileFavoriteRequest){
        return fileService.removeFromFavorites(fileFavoriteRequest);
    }

    @GetMapping("/favorite")
    public ResponseEntity<?> getFavoriteFiles(@RequestParam("username") String username){
        return new ResponseEntity<>(new ListOfData(fileService.getFavoriteFiles(username)), HttpStatus.OK);
    }

}
