package com.java_web_service_qlkh_cdda.service.impl;

import com.java_web_service_qlkh_cdda.dto.request.GradeRequest;
import com.java_web_service_qlkh_cdda.dto.response.SubmissionResponse;
import com.java_web_service_qlkh_cdda.entity.Submission;
import com.java_web_service_qlkh_cdda.entity.User;
import com.java_web_service_qlkh_cdda.enums.StatusEnum;
import com.java_web_service_qlkh_cdda.exception.InvalidStateException;
import com.java_web_service_qlkh_cdda.exception.ResourceNotFoundException;
import com.java_web_service_qlkh_cdda.repository.SubmissionRepository;
import com.java_web_service_qlkh_cdda.repository.UserRepository;
import com.java_web_service_qlkh_cdda.service.GradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradeServiceImpl implements GradeService {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final SubmissionServiceImpl submissionService;

    @Override
    @Transactional
    public SubmissionResponse gradeSubmission(GradeRequest request, String lecturerUsername) {
        Submission submission = submissionRepository.findById(request.getSubmissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Submission", request.getSubmissionId()));

        // Business rule: can only grade SUBMITTED or LATE submissions
        if (submission.getStatus() == StatusEnum.PENDING) {
            throw new InvalidStateException("Cannot grade a submission that has not been submitted yet");
        }

        User lecturer = userRepository.findByUsername(lecturerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer not found: " + lecturerUsername));

        if (request.getScore() > submission.getAssignment().getMaxScore()) {
            throw new InvalidStateException("Score cannot exceed max score of " + submission.getAssignment().getMaxScore());
        }

        submission.setScore(request.getScore());
        submission.setFeedback(request.getFeedback());
        submission.setStatus(StatusEnum.GRADED);
        submission.setGradedBy(lecturer);
        submission.setGradedAt(LocalDateTime.now());

        Submission saved = submissionRepository.save(submission);
        // AOP @AfterReturning will log: "Lecturer X graded Submission Y with Score Z"
        log.info("[GRADE] Lecturer '{}' graded Submission ID: {} with Score: {}",
                lecturerUsername, saved.getId(), saved.getScore());

        return submissionService.toResponse(saved);
    }

    @Override
    @Transactional
    public SubmissionResponse returnSubmission(Long submissionId, String feedback, String lecturerUsername) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", submissionId));

        if (submission.getStatus() != StatusEnum.SUBMITTED && submission.getStatus() != StatusEnum.LATE) {
            throw new InvalidStateException("Can only return SUBMITTED or LATE submissions");
        }

        // Reset to PENDING so student can re-submit
        submission.setStatus(StatusEnum.PENDING);
        submission.setFeedback(feedback);
        submission.setReportUrl(null);
        submission.setGithubUrl(null);

        Submission saved = submissionRepository.save(submission);
        log.info("[GRADE] Lecturer '{}' returned Submission ID: {} for re-submission", lecturerUsername, submissionId);
        return submissionService.toResponse(saved);
    }
}