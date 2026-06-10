package com.java_web_service_qlkh_cdda.service;

import com.java_web_service_qlkh_cdda.dto.request.*;
import com.java_web_service_qlkh_cdda.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(String username, String password);
    AuthResponse refreshToken(String refreshToken);
    void logout(String accessToken, String refreshToken);
    void register(RegisterRequest request);
    void changePassword(String username, ChangePasswordRequest request);
    void forgotPassword(String email);
    void resetPassword(ResetPasswordRequest request);
}