package com.moneyfi.apigateway.service.common;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface S3AwsService {
    String uploadUserProfilePictureToS3(Long userIdByUsername, String username, MultipartFile file);

    ResponseEntity<ByteArrayResource> fetchUserProfilePictureFromS3(Long userId, String username);

    ResponseEntity<String> deleteProfilePictureFromS3(Long userIdByUsername, String username);

    String uploadDefectPictureByUser(String imageId, MultipartFile file);
}
