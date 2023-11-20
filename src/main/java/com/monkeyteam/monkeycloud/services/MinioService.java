package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.repositories.FolderRepository;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class MinioService {
    private final FolderRepository folderRepository;
    private final MinioClient minioClient;

    public ResponseEntity<?> createBucket(String username) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(username).build());
        return new ResponseEntity<>(HttpStatus.OK.value(), HttpStatus.OK);
    }
}
