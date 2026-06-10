package com.java_web_service_qlkh_cdda.service;

import com.java_web_service_qlkh_cdda.dto.response.EnrollmentResponse;
import com.java_web_service_qlkh_cdda.dto.response.PageResponse;

public interface EnrollmentService {
    EnrollmentResponse enrollStudent(Long studentId, Long courseId);
    void unenrollStudent(Long studentId, Long courseId);
    PageResponse<EnrollmentResponse> getEnrollmentsByStudent(Long studentId, int page, int size);
    PageResponse<EnrollmentResponse> getEnrollmentsByCourse(Long courseId, int page, int size);
}