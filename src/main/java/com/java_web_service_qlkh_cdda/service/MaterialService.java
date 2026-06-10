package com.java_web_service_qlkh_cdda.service;

import com.java_web_service_qlkh_cdda.dto.response.MaterialResponse;
import com.java_web_service_qlkh_cdda.dto.response.PageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MaterialService {
    MaterialResponse uploadMaterial(Long courseId, String title, String description,
                                    MultipartFile file, String lecturerUsername);
    MaterialResponse getMaterialById(Long id);
    void deleteMaterial(Long id, String lecturerUsername);
    PageResponse<MaterialResponse> getMaterialsByCourse(Long courseId, int page, int size);
}