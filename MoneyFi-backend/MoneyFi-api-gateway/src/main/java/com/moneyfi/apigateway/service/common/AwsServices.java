package com.moneyfi.apigateway.service.common;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.multipart.MultipartFile;

public interface AwsServices {
    String uploadPictureToS3(Long id, String username, MultipartFile file, String uploadPurpose);

    ResponseEntity<ByteArrayResource> fetchUserProfilePictureFromS3(Long id, String username);

    ResponseEntity<String> deleteProfilePictureFromS3(Long id, String username);

    String uploadDefectPictureByUser(String imageId, MultipartFile file);

    void sendEmailToUserUsingAwsSes(SimpleMailMessage simpleMailMessage);
}
