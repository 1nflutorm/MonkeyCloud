package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.dtos.BucketDto;
import com.monkeyteam.monkeycloud.dtos.ListOfData;
import com.monkeyteam.monkeycloud.dtos.UserDto;
import com.monkeyteam.monkeycloud.entities.User;
import com.monkeyteam.monkeycloud.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    public ResponseEntity<?> getFilesByAdmin(){
        List<BucketDto> bucketList = new ArrayList<>();
        userRepository.GetAllUsers().forEach(user -> bucketList.add(new BucketDto(user)));
        return ResponseEntity.ok(new ListOfData(bucketList));
    }
}
