package com.moneyfi.apigateway.service.common.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.moneyfi.apigateway.exceptions.S3AwsErrorThrowException;
import com.moneyfi.apigateway.service.common.S3AwsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class S3AwsServiceImpl implements S3AwsService {

    @Value("${application.bucket.name}")
    private String bucketName;

    private final AmazonS3 s3Client;

    public S3AwsServiceImpl(AmazonS3 s3Client){
        this.s3Client = s3Client;
    }

    @Override
    public String uploadUserProfilePictureToS3(Long userId, String username, MultipartFile file) {
        File fileObj = null;

        try {
            fileObj = convertMultiPartFileToFile(file);
            String fileName = generateFileNameForUserProfilePicture(userId, username);

            s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
            return "Profile Picture Uploaded!";
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            throw new S3AwsErrorThrowException("Exception occurred while uploading profile picture");
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
    public ResponseEntity<ByteArrayResource> fetchUserProfilePictureFromS3(Long userId, String username) {

        try {
            S3Object s3Object = s3Client.getObject(bucketName,
                    generateFileNameForUserProfilePicture(userId, username));
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
            throw new S3AwsErrorThrowException("Exception occurred while fetching profile picture");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public ResponseEntity<String> deleteProfilePictureFromS3(Long userId, String username) {
        try {
            s3Client.deleteObject(bucketName,
                    generateFileNameForUserProfilePicture(userId, username));
            return ResponseEntity.ok().body("profile_picture_removed");
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            throw new S3AwsErrorThrowException("Exception occurred while deleting the profile picture");
        }
    }

    @Override
    public String uploadDefectPictureByUser(String fileName, MultipartFile file) {
        File fileObj = null;

        try {
            fileObj = convertMultiPartFileToFile(file);
            s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
            return "Profile Picture Uploaded!";
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            throw new S3AwsErrorThrowException("Exception occurred while uploading defect image");
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

    private File convertMultiPartFileToFile(MultipartFile file){
        File convertedFile = new File(file.getOriginalFilename());

        try (FileOutputStream fos = new FileOutputStream(convertedFile)){
            fos.write(file.getBytes());
        } catch (IOException e){
            e.printStackTrace();
        }
        return convertedFile;
    }

    private String generateFileNameForUserProfilePicture(Long userId, String username){
        return "profile_pic_" + (userId+143) +
                username.substring(0,username.indexOf('@'));
    }

    private String generateFileNameForUserDefectRequest(String imageId, String username){
        return "user_request_" + (imageId) +
                username.substring(0,username.indexOf('@'));
    }
}
