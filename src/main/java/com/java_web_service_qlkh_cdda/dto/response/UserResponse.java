package com.java_web_service_qlkh_cdda.dto.response;

import com.java_web_service_qlkh_cdda.enums.RoleEnum;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private RoleEnum role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}