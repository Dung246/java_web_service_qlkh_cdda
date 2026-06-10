package com.java_web_service_qlkh_cdda.service.impl;

import com.java_web_service_qlkh_cdda.dto.response.EnrollmentResponse;
import com.java_web_service_qlkh_cdda.dto.response.PageResponse;
import com.java_web_service_qlkh_cdda.entity.Course;
import com.java_web_service_qlkh_cdda.entity.Enrollment;
import com.java_web_service_qlkh_cdda.entity.User;
import com.java_web_service_qlkh_cdda.exception.DuplicateResourceException;
import com.java_web_service_qlkh_cdda.exception.ResourceNotFoundException;
import com.java_web_service_qlkh_cdda.repository.CourseRepository;
import com.java_web_service_qlkh_cdda.repository.EnrollmentRepository;
import com.java_web_service_qlkh_cdda.repository.UserRepository;
import com.java_web_service_qlkh_cdda.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public EnrollmentResponse enrollStudent(Long studentId, Long courseId) {
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new DuplicateResourceException("Student is already enrolled in this course");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        if (!Boolean.TRUE.equals(course.getIsActive())) {
            throw new IllegalArgumentException("Cannot enroll in an inactive course");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .isActive(true)
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("[ENROLLMENT] Student '{}' enrolled in course '{}'", student.getUsername(), course.getCourseCode());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void unenrollStudent(Long studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        enrollmentRepository.delete(enrollment);
        log.info("[ENROLLMENT] Student id {} unenrolled from course id {}", studentId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EnrollmentResponse> getEnrollmentsByStudent(Long studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("enrolledAt").descending());
        Page<Enrollment> enrollmentPage = enrollmentRepository.findByStudentId(studentId, pageable);

        List<EnrollmentResponse> content = enrollmentPage.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return buildPage(content, enrollmentPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EnrollmentResponse> getEnrollmentsByCourse(Long courseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("enrolledAt").descending());
        Page<Enrollment> enrollmentPage = enrollmentRepository.findByCourseId(courseId, pageable);

        List<EnrollmentResponse> content = enrollmentPage.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return buildPage(content, enrollmentPage);
    }

    private EnrollmentResponse toResponse(Enrollment e) {
        return EnrollmentResponse.builder()
                .id(e.getId())
                .studentId(e.getStudent().getId())
                .studentName(e.getStudent().getFullName())
                .studentUsername(e.getStudent().getUsername())
                .courseId(e.getCourse().getId())
                .courseCode(e.getCourse().getCourseCode())
                .courseName(e.getCourse().getCourseName())
                .isActive(e.getIsActive())
                .enrolledAt(e.getEnrolledAt())
                .build();
    }

    private <T> PageResponse<T> buildPage(List<T> content, Page<?> page) {
        return PageResponse.<T>builder()
                .content(content)
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}