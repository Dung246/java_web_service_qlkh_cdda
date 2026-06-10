package com.java_web_service_qlkh_cdda.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.java_web_service_qlkh_cdda.exception.FileUploadException;
import com.java_web_service_qlkh_cdda.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "auto",
                            "use_filename", true,
                            "unique_filename", true
                    )
            );
            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("[CLOUDINARY] Uploaded to folder '{}' → {}", folder, secureUrl);
            return secureUrl;
        } catch (IOException e) {
            log.error("[CLOUDINARY] Upload failed: {}", e.getMessage());
            throw new FileUploadException("Failed to upload file to cloud storage: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("[CLOUDINARY] Deleted file: {}", publicId);
        } catch (IOException e) {
            log.error("[CLOUDINARY] Delete failed: {}", e.getMessage());
            throw new FileUploadException("Failed to delete file from cloud storage", e);
        }
    }
}