package com.moneyfi.user.service.general.cloudinary.impl;

import com.cloudinary.Cloudinary;
import com.moneyfi.user.exceptions.CloudinaryImageException;
import com.moneyfi.user.exceptions.ResourceNotFoundException;
import com.moneyfi.user.service.general.cloudinary.CloudinaryService;
import com.moneyfi.user.util.constants.StringConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import static com.moneyfi.user.util.constants.StringConstants.*;

@Slf4j
@Service
@Profile({LOCAL_PROFILE, PROD_PROFILE})
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
    private static final String RESOURCE_TYPE = "resource_type";
    private static final String OK = "ok";
    private static final String RESULT = "result";

    @Override
    public Map uploadPictureToCloudinary(MultipartFile file, Long id, String username, String uploadPurpose) {
        String fileName = StringConstants.generateFileNameForPictureUpload(id, username, uploadPurpose);
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
        String fileName = StringConstants.generateFileNameForPictureUpload(id, username, imageType);
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
        String fileName = StringConstants.generateFileNameForPictureUpload(userId, username, UPLOAD_PROFILE_PICTURE);
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

    @Override
    public void uploadExcelTemplateToCloudinary(MultipartFile file, String fileName) {
        try {
            this.cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(
                            PUBLIC_ID, fileName,
                            RESOURCE_TYPE, "raw"
                    )
            );
        } catch (Exception e) {
            throw new CloudinaryImageException(IMAGE_UPLOAD_FAILED_MESSAGE + e);
        }
    }

    @Override
    public ResponseEntity<byte[]> getExcelTemplateFromCloudinary(String fileName) {
        try {
            String fileUrl = cloudinary.url().resourceType("raw").secure(true).generate(fileName);
            try (InputStream inputStream = new BufferedInputStream(new URL(fileUrl).openStream())) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(inputStream.readAllBytes());
            }
        } catch (FileNotFoundException ex) {
            throw new ResourceNotFoundException(IMAGE_NOT_FOUND_MESSAGE);
        } catch (Exception e) {
            throw new CloudinaryImageException(IMAGE_GET_FAILED_MESSAGE, e);
        }
    }
}
