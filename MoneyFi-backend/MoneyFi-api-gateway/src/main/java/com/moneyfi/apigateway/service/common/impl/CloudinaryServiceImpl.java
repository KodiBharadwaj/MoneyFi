package com.moneyfi.apigateway.service.common.impl;

import com.cloudinary.Cloudinary;
import com.moneyfi.apigateway.exceptions.CloudinaryImageException;
import com.moneyfi.apigateway.exceptions.ResourceNotFoundException;
import com.moneyfi.apigateway.service.common.CloudinaryService;
import com.moneyfi.apigateway.util.constants.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import static com.moneyfi.apigateway.util.constants.StringUtils.UPLOAD_PROFILE_PICTURE;

@Slf4j
@Service
@Profile("local")
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(Cloudinary cloudinary){
        this.cloudinary = cloudinary;
    }

    private static final String IMAGE_UPLOAD_FAILED_MESSAGE = "Failed to upload: ";
    private static final String IMAGE_GET_FAILED_MESSAGE = "Failed to get: ";
    private static final String IMAGE_NOT_FOUND_MESSAGE = "Image not found";
    private static final String IMAGE_DELETED_SUCCESS_MESSAGE = "Profile picture deleted successfully from Cloudinary";
    private static final String IMAGE_DELETED_FAILURE_MESSAGE = "Error deleting profile picture from Cloudinary";
    private static final String PUBLIC_ID = "public_id";
    private static final String OK = "ok";
    private static final String RESULT = "result";

    @Override
    public Map uploadPictureToCloudinary(MultipartFile file, Long id, String username, String uploadPurpose) {
        String fileName = StringUtils.generateFileNameForPictureUpload(id, username, uploadPurpose);
        try {
            return this.cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(PUBLIC_ID, fileName)
            );
        } catch (Exception e) {
            throw new CloudinaryImageException(IMAGE_UPLOAD_FAILED_MESSAGE + e);
        }
    }

    @Override
    public byte[] getImageFromCloudinary(Long id, String username, String imageType) {
        String fileName = StringUtils.generateFileNameForPictureUpload(id, username, imageType);
        try (InputStream inputStream =
                     new URL(cloudinary.url().secure(true).generate(fileName)).openStream()) {
            return inputStream.readAllBytes();
        } catch (FileNotFoundException ex) {
            throw new ResourceNotFoundException(IMAGE_NOT_FOUND_MESSAGE);
        } catch (Exception e) {
            throw new CloudinaryImageException(IMAGE_GET_FAILED_MESSAGE + e);
        }
    }

    @Override
    public ResponseEntity<String> deleteProfilePictureFromCloudinary(Long userId, String username) {
        String fileName = StringUtils.generateFileNameForPictureUpload(userId, username, UPLOAD_PROFILE_PICTURE);
        try {
            Map result = this.cloudinary.uploader().destroy(fileName, Map.of());
            if (OK.equals(result.get(RESULT))) {
                return ResponseEntity.ok(IMAGE_DELETED_SUCCESS_MESSAGE);
            } else {
                return ResponseEntity.status(404).body(IMAGE_NOT_FOUND_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(IMAGE_DELETED_FAILURE_MESSAGE);
        }
    }
}
