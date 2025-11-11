package com.moneyfi.apigateway.service.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CloudinaryService {
    Map uploadPictureToCloudinary(MultipartFile file, Long id, String username, String uploadPurpose);

    byte[] getImageFromCloudinary(Long id, String username, String imageType);

    ResponseEntity<String> deleteProfilePictureFromCloudinary(Long userIdByUsername, String username);
}
