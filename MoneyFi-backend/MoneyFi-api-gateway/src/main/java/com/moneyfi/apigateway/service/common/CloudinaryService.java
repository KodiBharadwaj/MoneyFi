package com.moneyfi.apigateway.service.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CloudinaryService {
    Map uploadPictureToCloudinary(MultipartFile file, Long id, String username, String uploadPurpose);

    byte[] getUserProfileFromCloudinary(Long userId, String username);

    ResponseEntity<String> deleteProfilePictureFromCloudinary(Long userIdByUsername, String username);
}
