package com.java_web_service_qlkh_cdda.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @Size(max = 2000)
    private String description;

    @NotNull(message = "Deadline is required")
    @Future(message = "Deadline must be in the future")
    private LocalDateTime deadline;

    @DecimalMin(value = "1.0", message = "Max score must be at least 1")
    @DecimalMax(value = "100.0", message = "Max score must not exceed 100")
    private Double maxScore = 100.0;

    @NotNull(message = "Course ID is required")
    private Long courseId;
}