package com.moneyfi.user.service.general.storage;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    void uploadPicture(MultipartFile file, Long id, String username, String uploadPurpose);

    ResponseEntity<ByteArrayResource> fetchImage(Long id, String username, String imageType);

    ResponseEntity<String> deletePicture(Long userId, String username, String imageType);

    void uploadFile(MultipartFile file, String fileName);

    ResponseEntity<byte[]> fetchFile(String fileName);
}
