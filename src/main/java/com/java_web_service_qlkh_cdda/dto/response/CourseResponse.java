package com.java_web_service_qlkh_cdda.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {

    private Long id;
    private String courseCode;
    private String courseName;
    private String description;
    private Integer credit;
    private Boolean isActive;
    private Long lecturerId;
    private String lecturerName;
    private long enrollmentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}