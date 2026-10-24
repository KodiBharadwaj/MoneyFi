package com.moneyfi.user.service.general.storage.impl;

import com.azure.core.exception.AzureException;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.moneyfi.user.exceptions.FileStorageException;
import com.moneyfi.user.service.general.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static com.moneyfi.user.util.constants.StringConstants.generateFileNameForPictureUpload;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "azure")
public class AzureBlobFileStorageService implements FileStorageService {

    private final BlobContainerClient containerClient;

    public AzureBlobFileStorageService(BlobServiceClient blobServiceClient,
                                       @Value("${azure.storage.container-name}") String containerName) {
        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
    }

    @Override
    public void uploadPicture(MultipartFile file, Long id, String username, String uploadPurpose) {
        try (InputStream inputStream = file.getInputStream()) {
            BlobClient blobClient = containerClient.getBlobClient(generateFileNameForPictureUpload(id, username, uploadPurpose));
            blobClient.upload(inputStream, file.getSize(), Boolean.TRUE);

            if (file.getContentType() != null)
                blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(file.getContentType()));

        } catch (IOException | AzureException e) {
            e.printStackTrace();
            throw new FileStorageException("Exception occurred while uploading profile picture", e);
        }
    }

    @Override
    public ResponseEntity<ByteArrayResource> fetchImage(Long id, String username, String imageType) {
        try {
            BlobClient blobClient = containerClient.getBlobClient(generateFileNameForPictureUpload(id, username, imageType));
            byte[] data = blobClient.downloadContent().toBytes();
            ByteArrayResource resource = new ByteArrayResource(data);
            return ResponseEntity.ok()
                    .contentLength(data.length)
                    .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"profile_picture.jpg\"")
                    .body(resource);
        } catch (AzureException e) {
            e.printStackTrace();
            throw new FileStorageException("Exception occurred while fetching profile picture", e);
        }
    }

    @Override
    public ResponseEntity<String> deletePicture(Long userId, String username, String imageType) {
        try {
            containerClient.getBlobClient(generateFileNameForPictureUpload(userId, username, imageType)).delete();
            return ResponseEntity.ok().body("profile_picture_removed");
        } catch (AzureException e) {
            e.printStackTrace();
            throw new FileStorageException("Exception occurred while deleting the profile picture", e);
        }
    }

    @Override
    public void uploadFile(MultipartFile file, String fileName) {
        try (InputStream inputStream = file.getInputStream()) {
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            blobClient.upload(inputStream, file.getSize(), Boolean.TRUE);

            if (file.getContentType() != null)
                blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(file.getContentType()));

        } catch (IOException | AzureException e) {
            e.printStackTrace();
            throw new FileStorageException("Exception occurred while uploading Excel template", e);
        }
    }

    @Override
    public ResponseEntity<byte[]> fetchFile(String fileName) {
        try {
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            byte[] data = blobClient.downloadContent().toBytes();
            return ResponseEntity.ok().contentLength(data.length).header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"").body(data);
        } catch (AzureException e) {
            e.printStackTrace();
            throw new FileStorageException("Exception occurred while fetching Excel template", e);
        }
    }
}
