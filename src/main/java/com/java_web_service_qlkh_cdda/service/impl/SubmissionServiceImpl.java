package com.java_web_service_qlkh_cdda.service.impl;

import com.java_web_service_qlkh_cdda.dto.request.SubmissionRequest;
import com.java_web_service_qlkh_cdda.dto.response.PageResponse;
import com.java_web_service_qlkh_cdda.dto.response.SubmissionResponse;
import com.java_web_service_qlkh_cdda.entity.Assignment;
import com.java_web_service_qlkh_cdda.entity.Submission;
import com.java_web_service_qlkh_cdda.entity.User;
import com.java_web_service_qlkh_cdda.enums.StatusEnum;
import com.java_web_service_qlkh_cdda.exception.DuplicateResourceException;
import com.java_web_service_qlkh_cdda.exception.InvalidStateException;
import com.java_web_service_qlkh_cdda.exception.ResourceNotFoundException;
import com.java_web_service_qlkh_cdda.repository.AssignmentRepository;
import com.java_web_service_qlkh_cdda.repository.SubmissionRepository;
import com.java_web_service_qlkh_cdda.repository.UserRepository;
import com.java_web_service_qlkh_cdda.service.CloudinaryService;
import com.java_web_service_qlkh_cdda.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final AssignmentRepository assignmentRepository;
    private final CloudinaryService cloudinaryService;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    @Override
    @Transactional
    public SubmissionResponse submitAssignment(String studentUsername, SubmissionRequest request) {
        User student = userRepository.findByUsername(studentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentUsername));

        Assignment assignment = assignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", request.getAssignmentId()));

        // Check if already submitted
        if (submissionRepository.existsByStudentIdAndAssignmentId(student.getId(), assignment.getId())) {
            throw new DuplicateResourceException("You have already submitted this assignment");
        }

        // Determine status: late or submitted
        StatusEnum status = LocalDateTime.now().isAfter(assignment.getDeadline())
                ? StatusEnum.LATE
                : StatusEnum.SUBMITTED;

        Submission submission = Submission.builder()
                .student(student)
                .assignment(assignment)
                .course(assignment.getCourse())
                .githubUrl(request.getGithubUrl())
                .status(status)
                .build();

        Submission saved = submissionRepository.save(submission);
        log.info("[SUBMISSION] Student '{}' submitted assignment '{}'", studentUsername, assignment.getTitle());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public SubmissionResponse uploadReport(String studentUsername, Long assignmentId, MultipartFile file) {
        // Validate file type
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type. Only PDF and Word documents are allowed.");
        }

        User student = userRepository.findByUsername(studentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentUsername));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", assignmentId));

        // Get or create submission
        Submission submission = submissionRepository
                .findByStudentIdAndAssignmentId(student.getId(), assignmentId)
                .orElseGet(() -> {
                    StatusEnum status = LocalDateTime.now().isAfter(assignment.getDeadline())
                            ? StatusEnum.LATE : StatusEnum.SUBMITTED;
                    return Submission.builder()
                            .student(student)
                            .assignment(assignment)
                            .course(assignment.getCourse())
                            .status(status)
                            .build();
                });

        if (submission.getStatus() == StatusEnum.GRADED) {
            throw new InvalidStateException("Cannot re-upload report for a graded submission");
        }

        // Upload to Cloudinary
        String reportUrl = cloudinaryService.uploadFile(file, "reports/" + assignmentId);
        submission.setReportUrl(reportUrl);

        if (submission.getStatus() == StatusEnum.PENDING) {
            submission.setStatus(LocalDateTime.now().isAfter(assignment.getDeadline())
                    ? StatusEnum.LATE : StatusEnum.SUBMITTED);
        }

        Submission saved = submissionRepository.save(submission);
        log.info("[SUBMISSION] Report uploaded by '{}' for assignment '{}'", studentUsername, assignment.getTitle());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionById(Long id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", id));
        return toResponse(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SubmissionResponse> getMySubmissions(String username, int page, int size) {
        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<Submission> submissionPage = submissionRepository.findByStudentId(student.getId(), pageable);

        List<SubmissionResponse> content = submissionPage.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return buildPage(content, submissionPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SubmissionResponse> getSubmissionsByAssignment(Long assignmentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<Submission> submissionPage = submissionRepository.findByAssignmentId(assignmentId, pageable);

        List<SubmissionResponse> content = submissionPage.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return buildPage(content, submissionPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SubmissionResponse> getSubmissionsByCourse(Long courseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        Page<Submission> submissionPage = submissionRepository.findByCourseId(courseId, pageable);

        List<SubmissionResponse> content = submissionPage.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return buildPage(content, submissionPage);
    }

    public SubmissionResponse toResponse(Submission s) {
        return SubmissionResponse.builder()
                .id(s.getId())
                .studentId(s.getStudent().getId())
                .studentName(s.getStudent().getFullName())
                .studentUsername(s.getStudent().getUsername())
                .assignmentId(s.getAssignment().getId())
                .assignmentTitle(s.getAssignment().getTitle())
                .courseId(s.getCourse().getId())
                .courseName(s.getCourse().getCourseName())
                .reportUrl(s.getReportUrl())
                .githubUrl(s.getGithubUrl())
                .score(s.getScore())
                .feedback(s.getFeedback())
                .status(s.getStatus())
                .gradedById(s.getGradedBy() != null ? s.getGradedBy().getId() : null)
                .gradedByName(s.getGradedBy() != null ? s.getGradedBy().getFullName() : null)
                .gradedAt(s.getGradedAt())
                .submittedAt(s.getSubmittedAt())
                .updatedAt(s.getUpdatedAt())
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