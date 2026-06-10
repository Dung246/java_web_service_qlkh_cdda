package com.java_web_service_qlkh_cdda.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionRequest {

    @NotNull(message = "Assignment ID is required")
    private Long assignmentId;

    @Size(max = 500, message = "GitHub URL must not exceed 500 characters")
    private String githubUrl;
}