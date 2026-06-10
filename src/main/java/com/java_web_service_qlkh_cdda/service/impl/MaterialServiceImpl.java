package com.java_web_service_qlkh_cdda.service.impl;

import com.java_web_service_qlkh_cdda.dto.response.MaterialResponse;
import com.java_web_service_qlkh_cdda.dto.response.PageResponse;
import com.java_web_service_qlkh_cdda.entity.Course;
import com.java_web_service_qlkh_cdda.entity.Material;
import com.java_web_service_qlkh_cdda.entity.User;
import com.java_web_service_qlkh_cdda.exception.ResourceNotFoundException;
import com.java_web_service_qlkh_cdda.repository.CourseRepository;
import com.java_web_service_qlkh_cdda.repository.MaterialRepository;
import com.java_web_service_qlkh_cdda.repository.UserRepository;
import com.java_web_service_qlkh_cdda.service.CloudinaryService;
import com.java_web_service_qlkh_cdda.service.MaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public MaterialResponse uploadMaterial(Long courseId, String title, String description,
                                           MultipartFile file, String lecturerUsername) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        User lecturer = userRepository.findByUsername(lecturerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + lecturerUsername));

        String fileUrl = cloudinaryService.uploadFile(file, "materials/" + courseId);

        Material material = Material.builder()
                .title(title)
                .description(description)
                .fileUrl(fileUrl)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .course(course)
                .uploadedBy(lecturer)
                .build();

        Material saved = materialRepository.save(material);
        log.info("[MATERIAL] Lecturer '{}' uploaded material '{}' to course '{}'",
                lecturerUsername, title, course.getCourseCode());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialResponse getMaterialById(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material", id));
        return toResponse(material);
    }

    @Override
    @Transactional
    public void deleteMaterial(Long id, String lecturerUsername) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material", id));
        materialRepository.delete(material);
        log.info("[MATERIAL] Material id {} deleted by '{}'", id, lecturerUsername);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MaterialResponse> getMaterialsByCourse(Long courseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
        Page<Material> materialPage = materialRepository.findByCourseId(courseId, pageable);

        List<MaterialResponse> content = materialPage.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<MaterialResponse>builder()
                .content(content)
                .currentPage(materialPage.getNumber())
                .totalPages(materialPage.getTotalPages())
                .totalElements(materialPage.getTotalElements())
                .pageSize(materialPage.getSize())
                .first(materialPage.isFirst())
                .last(materialPage.isLast())
                .build();
    }

    private MaterialResponse toResponse(Material m) {
        return MaterialResponse.builder()
                .id(m.getId())
                .title(m.getTitle())
                .description(m.getDescription())
                .fileUrl(m.getFileUrl())
                .fileType(m.getFileType())
                .fileSize(m.getFileSize())
                .courseId(m.getCourse().getId())
                .courseName(m.getCourse().getCourseName())
                .uploadedById(m.getUploadedBy().getId())
                .uploadedByName(m.getUploadedBy().getFullName())
                .uploadedAt(m.getUploadedAt())
                .build();
    }
}