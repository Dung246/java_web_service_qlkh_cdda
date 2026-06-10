package com.java_web_service_qlkh_cdda.service.impl;

import com.java_web_service_qlkh_cdda.dto.request.AssignmentRequest;
import com.java_web_service_qlkh_cdda.dto.response.AssignmentResponse;
import com.java_web_service_qlkh_cdda.dto.response.PageResponse;
import com.java_web_service_qlkh_cdda.entity.Assignment;
import com.java_web_service_qlkh_cdda.entity.Course;
import com.java_web_service_qlkh_cdda.entity.User;
import com.java_web_service_qlkh_cdda.exception.ResourceNotFoundException;
import com.java_web_service_qlkh_cdda.repository.AssignmentRepository;
import com.java_web_service_qlkh_cdda.repository.CourseRepository;
import com.java_web_service_qlkh_cdda.repository.UserRepository;
import com.java_web_service_qlkh_cdda.service.AssignmentService;
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
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AssignmentResponse createAssignment(AssignmentRequest request, String lecturerUsername) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", request.getCourseId()));

        User lecturer = userRepository.findByUsername(lecturerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + lecturerUsername));

        Assignment assignment = Assignment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .maxScore(request.getMaxScore() != null ? request.getMaxScore() : 100.0)
                .course(course)
                .createdBy(lecturer)
                .build();

        Assignment saved = assignmentRepository.save(assignment);
        log.info("[ASSIGNMENT] Created '{}' in course '{}'", saved.getTitle(), course.getCourseCode());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentById(Long id) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", id));
        return toResponse(assignment);
    }

    @Override
    @Transactional
    public AssignmentResponse updateAssignment(Long id, AssignmentRequest request) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", id));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", request.getCourseId()));

        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setDeadline(request.getDeadline());
        assignment.setMaxScore(request.getMaxScore() != null ? request.getMaxScore() : 100.0);
        assignment.setCourse(course);

        return toResponse(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public void deleteAssignment(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Assignment", id);
        }
        assignmentRepository.deleteById(id);
        log.info("[ASSIGNMENT] Deleted assignment id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AssignmentResponse> getAssignmentsByCourse(Long courseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("deadline").ascending());
        Page<Assignment> assignmentPage = assignmentRepository.findByCourseId(courseId, pageable);

        List<AssignmentResponse> content = assignmentPage.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<AssignmentResponse>builder()
                .content(content)
                .currentPage(assignmentPage.getNumber())
                .totalPages(assignmentPage.getTotalPages())
                .totalElements(assignmentPage.getTotalElements())
                .pageSize(assignmentPage.getSize())
                .first(assignmentPage.isFirst())
                .last(assignmentPage.isLast())
                .build();
    }

    private AssignmentResponse toResponse(Assignment a) {
        return AssignmentResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .description(a.getDescription())
                .deadline(a.getDeadline())
                .maxScore(a.getMaxScore())
                .courseId(a.getCourse().getId())
                .courseName(a.getCourse().getCourseName())
                .courseCode(a.getCourse().getCourseCode())
                .createdById(a.getCreatedBy() != null ? a.getCreatedBy().getId() : null)
                .createdByName(a.getCreatedBy() != null ? a.getCreatedBy().getFullName() : null)
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}