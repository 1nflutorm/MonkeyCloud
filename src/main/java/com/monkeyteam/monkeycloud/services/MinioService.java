package com.monkeyteam.monkeycloud.services;

import com.monkeyteam.monkeycloud.repositories.FolderRepository;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
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
    private final MinioClient minioClient;
    public static final long LIMIT_SIZE = 1073741824;

    public ResponseEntity<?> createBucket(String username) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(username).build());
        return new ResponseEntity<>(HttpStatus.OK.value(), HttpStatus.OK);
    }

    public long getSizeOfBucket(String username) {
        long size = 0;
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(username)
                .recursive(true)
                .build());
        for (Result<Item> item : results) {
            try {
                size += item.get().size();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return size;
    }
}
