package com.java_web_service_qlkh_cdda.controller;

import com.java_web_service_qlkh_cdda.dto.request.ChangePasswordRequest;
import com.java_web_service_qlkh_cdda.dto.request.ForgotPasswordRequest;
import com.java_web_service_qlkh_cdda.dto.request.LoginRequest;
import com.java_web_service_qlkh_cdda.dto.request.LogoutRequest;
import com.java_web_service_qlkh_cdda.dto.request.RefreshTokenRequest;
import com.java_web_service_qlkh_cdda.dto.request.RegisterRequest;
import com.java_web_service_qlkh_cdda.dto.request.ResetPasswordRequest;
import com.java_web_service_qlkh_cdda.dto.response.ApiResponse;
import com.java_web_service_qlkh_cdda.dto.response.AuthResponse;
import com.java_web_service_qlkh_cdda.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication",
        description = "Dang ky, Dang nhap, Refresh Token, Dang xuat, Doi/Quen mat khau")
public class AuthController {

    private final AuthService authService;

    // ─── FR-01: ĐĂNG NHẬP ─────────────────────────────────────────────────────
    @PostMapping("/login")
    @Operation(
            summary = "Dang nhap he thong",
            description = "Xac thuc username/password. " +
                    "Tra ve AccessToken (30 phut) va RefreshToken (7 ngay). " +
                    "Dinh kem: Authorization: Bearer <accessToken>"
    )
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success("Dang nhap thanh cong", response));
    }

    // ─── FR-04: ĐĂNG KÝ ───────────────────────────────────────────────────────
    @PostMapping("/register")
    @Operation(
            summary = "Dang ky tai khoan Sinh vien",
            description = "Tao tai khoan moi voi role STUDENT. Mat khau ma hoa BCrypt strength=10."
    )
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request) {

        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dang ky thanh cong. Vui long dang nhap.", null));
    }

    // ─── FR-02: REFRESH TOKEN ─────────────────────────────────────────────────
    @PostMapping("/refresh")
    @Operation(
            summary = "Lam moi Access Token",
            description = "Dung RefreshToken con han de cap AccessToken moi. " +
                    "Nguoi dung khong can dang nhap lai."
    )
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token duoc lam moi thanh cong", response));
    }

    // ─── FR-03: ĐĂNG XUẤT — BLACKLIST CẢ 2 TOKEN ─────────────────────────────
    @PostMapping("/logout")
    @Operation(
            summary = "Dang xuat",
            description = "Blacklist ca AccessToken va RefreshToken. " +
                    "Moi request sau dung token nay se bi tu choi 401."
    )
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) LogoutRequest body) {

        String refreshToken = (body != null) ? body.getRefreshToken() : null;
        authService.logout(authHeader, refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Dang xuat thanh cong", null));
    }

    // ─── FR-10: QUÊN MẬT KHẨU ────────────────────────────────────────────────
    @PostMapping("/forgot-password")
    @Operation(summary = "Quen mat khau")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(
                ApiResponse.success("Huong dan dat lai mat khau da gui toi email", null));
    }

    // ─── FR-10: RESET MẬT KHẨU ───────────────────────────────────────────────
    @PostMapping("/reset-password")
    @Operation(summary = "Dat lai mat khau")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Mat khau da duoc dat lai thanh cong", null));
    }
}