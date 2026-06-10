package com.java_web_service_qlkh_cdda.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GradeRequest {

    @NotNull(message = "Submission ID is required")
    private Long submissionId;

    @NotNull(message = "Score is required")
    @DecimalMin(value = "0.0", message = "Score cannot be negative")
    @DecimalMax(value = "100.0", message = "Score cannot exceed 100")
    private Double score;

    @Size(max = 2000, message = "Feedback must not exceed 2000 characters")
    private String feedback;
}