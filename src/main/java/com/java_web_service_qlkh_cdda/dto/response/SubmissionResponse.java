package com.java_web_service_qlkh_cdda.dto.response;

import com.java_web_service_qlkh_cdda.enums.StatusEnum;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private String studentUsername;
    private Long assignmentId;
    private String assignmentTitle;
    private Long courseId;
    private String courseName;
    private String reportUrl;
    private String githubUrl;
    private Double score;
    private String feedback;
    private StatusEnum status;
    private Long gradedById;
    private String gradedByName;
    private LocalDateTime gradedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
}