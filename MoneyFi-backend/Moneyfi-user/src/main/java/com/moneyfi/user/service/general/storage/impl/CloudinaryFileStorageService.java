package com.moneyfi.user.service.general.storage.impl;

import com.cloudinary.Cloudinary;
import com.moneyfi.user.exceptions.CloudinaryImageException;
import com.moneyfi.user.exceptions.ResourceNotFoundException;
import com.moneyfi.user.service.general.storage.FileStorageService;
import com.moneyfi.user.util.constants.StringConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
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

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "cloudinary", matchIfMissing = true)
public class CloudinaryFileStorageService implements FileStorageService {

    private final Cloudinary cloudinary;

    public CloudinaryFileStorageService(Cloudinary cloudinary){
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
    public void uploadPicture(MultipartFile file, Long id, String username, String uploadPurpose) {
        String fileName = StringConstants.generateFileNameForPictureUpload(id, username, uploadPurpose);
        try {
            this.cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(PUBLIC_ID, fileName)
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new CloudinaryImageException(IMAGE_UPLOAD_FAILED_MESSAGE + e);
        }
    }

    @Override
    public ResponseEntity<ByteArrayResource> fetchImage(Long id, String username, String imageType) {
        String fileName = StringConstants.generateFileNameForPictureUpload(id, username, imageType);
        try (InputStream inputStream =
                     new URL(cloudinary.url().secure(true).generate(fileName)).openStream()) {
            byte[] imageBytes = inputStream.readAllBytes();
            ByteArrayResource resource = new ByteArrayResource(imageBytes);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .contentLength(imageBytes.length)
                    .body(resource);
        } catch (FileNotFoundException ex) {
            throw new ResourceNotFoundException(IMAGE_NOT_FOUND_MESSAGE);
        } catch (Exception e) {
            throw new CloudinaryImageException(IMAGE_GET_FAILED_MESSAGE + e);
        }
    }

    @Override
    public ResponseEntity<String> deletePicture(Long userId, String username, String imageType) {
        String fileName = StringConstants.generateFileNameForPictureUpload(userId, username, imageType);
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
    public void uploadFile(MultipartFile file, String fileName) {
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
    public ResponseEntity<byte[]> fetchFile(String fileName) {
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
