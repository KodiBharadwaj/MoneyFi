package com.moneyfi.user.service.general.storage.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.moneyfi.user.exceptions.FileStorageException;
import com.moneyfi.user.service.general.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import static com.moneyfi.user.util.constants.StringConstants.generateFileNameForPictureUpload;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "aws")
public class S3FileStorageService implements FileStorageService {

    @Value("${application.bucket.name}")
    private String bucketName;

    private final AmazonS3 s3Client;

    public S3FileStorageService(AmazonS3 s3Client){
        this.s3Client = s3Client;
    }

    @Override
    public void uploadPicture(MultipartFile file, Long id, String username, String uploadPurpose) {
        File fileObj = null;

        try {
            fileObj = convertMultiPartFileToFile(file);
            s3Client.putObject(new PutObjectRequest(bucketName, generateFileNameForPictureUpload(id, username, uploadPurpose), fileObj));
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            throw new FileStorageException("Exception occurred while uploading profile picture");
        } finally {
            if (fileObj != null && fileObj.exists()) {
                try {
                    Files.delete(fileObj.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public ResponseEntity<ByteArrayResource> fetchImage(Long id, String username, String imageType) {
        try {
            S3Object s3Object = s3Client.getObject(bucketName,generateFileNameForPictureUpload(id, username, imageType));
            S3ObjectInputStream inputStream = s3Object.getObjectContent();
            byte[] data = IOUtils.toByteArray(inputStream);
            ByteArrayResource resource = new ByteArrayResource(data);
            return ResponseEntity
                    .ok()
                    .contentLength(data.length)
                    .header("Content-type", "application/octet-stream")
                    .header("Content-disposition", "attachment; filename=\"" + "profile_picture" + ".jpg" + "\"")
                    .body(resource);
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            throw new FileStorageException("Exception occurred while fetching profile picture");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ResponseEntity<String> deletePicture(Long userId, String username, String imageType) {
        try {
            s3Client.deleteObject(bucketName, generateFileNameForPictureUpload(userId, username, imageType));
            return ResponseEntity.ok().body("profile_picture_removed");
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            throw new FileStorageException("Exception occurred while deleting the profile picture");
        }
    }

    @Override
    public void uploadFile(MultipartFile file, String fileName) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            s3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);
        } catch (IOException | AmazonServiceException e) {
            throw new FileStorageException("Exception occurred while uploading Excel template", e);
        }
    }

    @Override
    public ResponseEntity<byte[]> fetchFile(String fileName) {
        try {
            S3Object s3Object = s3Client.getObject(bucketName, fileName);
            try (S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
                byte[] data = IOUtils.toByteArray(inputStream);
                return ResponseEntity.ok()
                        .contentLength(data.length)
                        .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .body(data);
            }
        } catch (AmazonServiceException e) {
            throw new FileStorageException("Exception occurred while fetching Excel template", e);
        } catch (IOException e) {
            throw new FileStorageException("Error reading Excel template from S3", e);
        }
    }

    private File convertMultiPartFileToFile(MultipartFile file){
        File convertedFile = new File(file.getOriginalFilename());

        try (FileOutputStream fos = new FileOutputStream(convertedFile)){
            fos.write(file.getBytes());
        } catch (IOException e){
            e.printStackTrace();
        }
        return convertedFile;
    }
}
