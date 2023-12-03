package com.monkeyteam.monkeycloud.controllers;

import com.monkeyteam.monkeycloud.dtos.searchDtos.PrivateSearchByDate;
import com.monkeyteam.monkeycloud.dtos.searchDtos.PrivateSearchByFileName;
import com.monkeyteam.monkeycloud.services.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SearchController {

    private SearchService searchService;

    @Autowired
    public void setSearchService (SearchService searchService){
        this.searchService = searchService;
    }
    @GetMapping("/private-search-by-filename")
    public ResponseEntity<?> privateSearchByFileName(@RequestBody PrivateSearchByFileName privateSearchByFileName){
        return searchService.privateSearchByFileName(privateSearchByFileName.getUsername(), privateSearchByFileName.getFilename());
    }

    @GetMapping("/private-search-by-date")
    public ResponseEntity<?> privateSearchByDate(@RequestBody PrivateSearchByDate privateSearchByDate){
        return searchService.privateSearchByDate(privateSearchByDate.getUsername(), privateSearchByDate.getDate());
    }

    @GetMapping("/public-search-by-filename")
    public ResponseEntity<?> publicSearchByFileName(@RequestBody PrivateSearchByDate privateSearchByDate){
        return searchService.privateSearchByDate(privateSearchByDate.getUsername(), privateSearchByDate.getDate());
    }

    @GetMapping("/public-search-by-date")
    public ResponseEntity<?> publicSearchByDate(@RequestBody PrivateSearchByDate privateSearchByDate){
        return searchService.privateSearchByDate(privateSearchByDate.getUsername(), privateSearchByDate.getDate());
    }

    @GetMapping("/public-search-by-username")
    public ResponseEntity<?> publicSearchByUsername(@RequestBody PrivateSearchByDate privateSearchByDate){
        return searchService.privateSearchByDate(privateSearchByDate.getUsername(), privateSearchByDate.getDate());
    }

}
