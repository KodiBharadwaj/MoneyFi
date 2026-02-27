package com.moneyfi.user.service.common;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface AwsServices {
    String uploadPictureToS3(Long id, String username, MultipartFile file, String uploadPurpose);

    ResponseEntity<ByteArrayResource> fetchUserProfilePictureFromS3(Long id, String username);

    ResponseEntity<String> deleteProfilePictureFromS3(Long id, String username);
}
