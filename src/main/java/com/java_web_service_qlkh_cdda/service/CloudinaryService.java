package com.java_web_service_qlkh_cdda.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    String uploadFile(MultipartFile file, String folder);
    void deleteFile(String publicId);
}