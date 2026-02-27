package com.moneyfi.user.service.common.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.moneyfi.user.exceptions.S3AwsErrorThrowException;
import com.moneyfi.user.service.common.AwsServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import static com.moneyfi.user.util.constants.StringConstants.UPLOAD_PROFILE_PICTURE;
import static com.moneyfi.user.util.constants.StringConstants.generateFileNameForPictureUpload;

@Service
public class AwsServicesImpl implements AwsServices {

    @Value("${application.bucket.name}")
    private String bucketName;

    private final AmazonS3 s3Client;

    public AwsServicesImpl(AmazonS3 s3Client){
        this.s3Client = s3Client;
    }

    @Override
    public String uploadPictureToS3(Long id, String username, MultipartFile file, String uploadPurpose) {
        File fileObj = null;

        try {
            fileObj = convertMultiPartFileToFile(file);
            s3Client.putObject(new PutObjectRequest(bucketName, generateFileNameForPictureUpload(id, username, uploadPurpose), fileObj));
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
    public ResponseEntity<ByteArrayResource> fetchUserProfilePictureFromS3(Long id, String username) {

        try {
            S3Object s3Object = s3Client.getObject(bucketName,generateFileNameForPictureUpload(id, username, UPLOAD_PROFILE_PICTURE));
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
            s3Client.deleteObject(bucketName, generateFileNameForPictureUpload(userId, username, UPLOAD_PROFILE_PICTURE));
            return ResponseEntity.ok().body("profile_picture_removed");
        } catch (AmazonServiceException e) {
            e.printStackTrace();
            throw new S3AwsErrorThrowException("Exception occurred while deleting the profile picture");
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
