package com.java_web_service_qlkh_cdda.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequest {

    @NotBlank(message = "Course code is required")
    @Size(max = 20, message = "Course code must not exceed 20 characters")
    private String courseCode;

    @NotBlank(message = "Course name is required")
    @Size(max = 200, message = "Course name must not exceed 200 characters")
    private String courseName;

    @Size(max = 1000)
    private String description;

    @NotNull(message = "Credit is required")
    @Min(value = 1, message = "Credit must be at least 1")
    @Max(value = 10, message = "Credit must not exceed 10")
    private Integer credit;

    private Long lecturerId;

    private Boolean isActive = true;
}