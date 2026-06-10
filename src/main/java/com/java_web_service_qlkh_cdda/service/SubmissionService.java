package com.java_web_service_qlkh_cdda.service;

import com.java_web_service_qlkh_cdda.dto.request.SubmissionRequest;
import com.java_web_service_qlkh_cdda.dto.response.PageResponse;
import com.java_web_service_qlkh_cdda.dto.response.SubmissionResponse;
import org.springframework.web.multipart.MultipartFile;

public interface SubmissionService {
    SubmissionResponse submitAssignment(String studentUsername, SubmissionRequest request);
    SubmissionResponse uploadReport(String studentUsername, Long assignmentId, MultipartFile file);
    SubmissionResponse getSubmissionById(Long id);
    PageResponse<SubmissionResponse> getMySubmissions(String username, int page, int size);
    PageResponse<SubmissionResponse> getSubmissionsByAssignment(Long assignmentId, int page, int size);
    PageResponse<SubmissionResponse> getSubmissionsByCourse(Long courseId, int page, int size);
}