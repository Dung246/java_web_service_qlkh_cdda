package com.java_web_service_qlkh_cdda.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;
}