package com.java_web_service_qlkh_cdda.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private String studentUsername;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Boolean isActive;
    private LocalDateTime enrolledAt;
}