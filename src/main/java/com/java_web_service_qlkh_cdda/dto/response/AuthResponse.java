package com.java_web_service_qlkh_cdda.dto.response;

import com.java_web_service_qlkh_cdda.enums.RoleEnum;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private RoleEnum role;
    private String accessToken;
    private String refreshToken;
    private long accessTokenExpiresIn;
    private String tokenType = "Bearer";
}