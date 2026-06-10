package com.java_web_service_qlkh_cdda.service;

import com.java_web_service_qlkh_cdda.dto.request.AssignmentRequest;
import com.java_web_service_qlkh_cdda.dto.response.AssignmentResponse;
import com.java_web_service_qlkh_cdda.dto.response.PageResponse;

public interface AssignmentService {
    AssignmentResponse createAssignment(AssignmentRequest request, String lecturerUsername);
    AssignmentResponse getAssignmentById(Long id);
    AssignmentResponse updateAssignment(Long id, AssignmentRequest request);
    void deleteAssignment(Long id);
    PageResponse<AssignmentResponse> getAssignmentsByCourse(Long courseId, int page, int size);
}