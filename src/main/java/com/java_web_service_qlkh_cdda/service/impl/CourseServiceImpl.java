package com.java_web_service_qlkh_cdda.service.impl;

import com.java_web_service_qlkh_cdda.dto.request.CourseRequest;
import com.java_web_service_qlkh_cdda.dto.response.CourseResponse;
import com.java_web_service_qlkh_cdda.dto.response.PageResponse;
import com.java_web_service_qlkh_cdda.entity.Course;
import com.java_web_service_qlkh_cdda.entity.User;
import com.java_web_service_qlkh_cdda.exception.DuplicateResourceException;
import com.java_web_service_qlkh_cdda.exception.ResourceNotFoundException;
import com.java_web_service_qlkh_cdda.repository.CourseRepository;
import com.java_web_service_qlkh_cdda.repository.EnrollmentRepository;
import com.java_web_service_qlkh_cdda.repository.UserRepository;
import com.java_web_service_qlkh_cdda.service.CourseService;
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
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        if (courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new DuplicateResourceException("Course code already exists: " + request.getCourseCode());
        }

        User lecturer = null;
        if (request.getLecturerId() != null) {
            lecturer = userRepository.findById(request.getLecturerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lecturer", request.getLecturerId()));
        }

        Course course = Course.builder()
                .courseCode(request.getCourseCode())
                .courseName(request.getCourseName())
                .description(request.getDescription())
                .credit(request.getCredit())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .lecturer(lecturer)
                .build();

        Course saved = courseRepository.save(course);
        log.info("[COURSE] Created course: {}", saved.getCourseCode());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
        return toResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        if (!course.getCourseCode().equals(request.getCourseCode())
                && courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new DuplicateResourceException("Course code already exists: " + request.getCourseCode());
        }

        User lecturer = null;
        if (request.getLecturerId() != null) {
            lecturer = userRepository.findById(request.getLecturerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lecturer", request.getLecturerId()));
        }

        course.setCourseCode(request.getCourseCode());
        course.setCourseName(request.getCourseName());
        course.setDescription(request.getDescription());
        course.setCredit(request.getCredit());
        course.setLecturer(lecturer);
        if (request.getIsActive() != null) course.setIsActive(request.getIsActive());

        return toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course", id);
        }
        courseRepository.deleteById(id);
        log.info("[COURSE] Deleted course id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getAllCourses(int page, int size, String keyword, Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Course> coursePage = courseRepository.searchCourses(keyword, isActive, pageable);

        List<CourseResponse> content = coursePage.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<CourseResponse>builder()
                .content(content)
                .currentPage(coursePage.getNumber())
                .totalPages(coursePage.getTotalPages())
                .totalElements(coursePage.getTotalElements())
                .pageSize(coursePage.getSize())
                .first(coursePage.isFirst())
                .last(coursePage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> getCoursesByLecturer(Long lecturerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Course> coursePage = courseRepository.findByLecturerId(lecturerId, pageable);

        List<CourseResponse> content = coursePage.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<CourseResponse>builder()
                .content(content)
                .currentPage(coursePage.getNumber())
                .totalPages(coursePage.getTotalPages())
                .totalElements(coursePage.getTotalElements())
                .pageSize(coursePage.getSize())
                .first(coursePage.isFirst())
                .last(coursePage.isLast())
                .build();
    }

    private CourseResponse toResponse(Course course) {
        long count = enrollmentRepository.countByCourseId(course.getId());
        return CourseResponse.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .credit(course.getCredit())
                .isActive(course.getIsActive())
                .lecturerId(course.getLecturer() != null ? course.getLecturer().getId() : null)
                .lecturerName(course.getLecturer() != null ? course.getLecturer().getFullName() : null)
                .enrollmentCount(count)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}