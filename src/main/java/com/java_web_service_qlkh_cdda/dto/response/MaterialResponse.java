package com.java_web_service_qlkh_cdda.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialResponse {

    private Long id;
    private String title;
    private String description;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private Long courseId;
    private String courseName;
    private Long uploadedById;
    private String uploadedByName;
    private LocalDateTime uploadedAt;
}