package com.java_web_service_qlkh_cdda.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private Double maxScore;
    private Long courseId;
    private String courseName;
    private String courseCode;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}