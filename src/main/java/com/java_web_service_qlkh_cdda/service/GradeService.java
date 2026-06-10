package com.java_web_service_qlkh_cdda.service;

import com.java_web_service_qlkh_cdda.dto.request.GradeRequest;
import com.java_web_service_qlkh_cdda.dto.response.SubmissionResponse;

public interface GradeService {
    SubmissionResponse gradeSubmission(GradeRequest request, String lecturerUsername);
    SubmissionResponse returnSubmission(Long submissionId, String feedback, String lecturerUsername);
}