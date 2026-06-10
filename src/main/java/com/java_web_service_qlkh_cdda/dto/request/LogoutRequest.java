package com.java_web_service_qlkh_cdda.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {
    // Gửi refreshToken lên body khi logout để blacklist cả 2
    private String refreshToken;
}