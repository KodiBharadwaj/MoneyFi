package com.moneyfi.apigateway.service.common.impl;

import com.cloudinary.Cloudinary;
import com.moneyfi.apigateway.service.common.CloudinaryService;
import com.moneyfi.apigateway.util.constants.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

@Slf4j
@Service
@Profile("local")
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(Cloudinary cloudinary){
        this.cloudinary = cloudinary;
    }

    @Override
    public Map uploadProfilePictureToCloudinary(MultipartFile file, Long userId, String username) {
        String fileName = StringUtils.generateFileNameForUserProfilePicture(userId, username);
        try {
            return this.cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of("public_id", fileName)
            );
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getUserProfileFromCloudinary(Long userId, String username) {
        String fileName = StringUtils.generateFileNameForUserProfilePicture(userId, username);
        try (InputStream inputStream =
                     new URL(cloudinary.url().secure(true).generate(fileName)).openStream()) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to download image from Cloudinary", e);
        }
    }

    @Override
    public ResponseEntity<String> deleteProfilePictureFromCloudinary(Long userId, String username) {
        String fileName = StringUtils.generateFileNameForUserProfilePicture(userId, username);
        try {
            Map result = this.cloudinary.uploader().destroy(fileName, Map.of());
            if ("ok".equals(result.get("result"))) {
                return ResponseEntity.ok("Profile picture deleted successfully from Cloudinary.");
            } else {
                return ResponseEntity.status(404).body("Profile picture not found in Cloudinary.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting profile picture from Cloudinary.");
        }
    }
}
