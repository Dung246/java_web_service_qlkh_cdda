package com.java_web_service_qlkh_cdda.service.impl;

import com.java_web_service_qlkh_cdda.dto.request.*;
import com.java_web_service_qlkh_cdda.dto.response.AuthResponse;
import com.java_web_service_qlkh_cdda.entity.TokenBlacklist;
import com.java_web_service_qlkh_cdda.entity.User;
import com.java_web_service_qlkh_cdda.enums.RoleEnum;
import com.java_web_service_qlkh_cdda.exception.DuplicateResourceException;
import com.java_web_service_qlkh_cdda.exception.InvalidStateException;
import com.java_web_service_qlkh_cdda.exception.ResourceNotFoundException;
import com.java_web_service_qlkh_cdda.repository.TokenBlacklistRepository;
import com.java_web_service_qlkh_cdda.repository.UserRepository;
import com.java_web_service_qlkh_cdda.security.CustomUserDetails;
import com.java_web_service_qlkh_cdda.security.CustomUserDetailsService;
import com.java_web_service_qlkh_cdda.service.AuthService;
import com.java_web_service_qlkh_cdda.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── UC-01: LOGIN ──────────────────────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponse login(String username, String password) {
        // Spring Security xác thực — ném BadCredentialsException nếu sai
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        CustomUserDetails customUser = (CustomUserDetails) userDetails;

        // Tạo cả 2 token
        String accessToken  = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        log.info("[AUTH] User '{}' logged in with role '{}'", username,
                customUser.getAuthorities().iterator().next().getAuthority());

        return buildAuthResponse(customUser, accessToken, refreshToken);
    }

    // ─── UC-02: REFRESH TOKEN ──────────────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        // Kiểm tra refresh token có bị revoke chưa
        if (tokenBlacklistRepository.existsByTokenString(refreshToken)) {
            throw new InvalidStateException("Refresh token has been revoked. Please login again.");
        }

        String username;
        try {
            username = jwtUtil.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new InvalidStateException("Invalid refresh token format.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtUtil.isTokenValid(refreshToken, userDetails)) {
            throw new InvalidStateException("Refresh token is expired or invalid. Please login again.");
        }

        CustomUserDetails customUser = (CustomUserDetails) userDetails;

        // Sinh AccessToken mới, giữ nguyên RefreshToken cũ (Refresh Token Rotation tuỳ chọn)
        String newAccessToken = jwtUtil.generateAccessToken(userDetails);

        log.info("[AUTH] Token refreshed for user '{}'", username);
        return buildAuthResponse(customUser, newAccessToken, refreshToken);
    }

    // ─── UC-03: LOGOUT — BLACKLIST CẢ 2 TOKEN ──────────────────────────────────
    @Override
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        revokeToken(accessToken);
        revokeToken(refreshToken);
        log.info("[AUTH] User logged out — both tokens revoked");
    }

    /**
     * Helper: blacklist một token bất kỳ (access hoặc refresh)
     */
    private void revokeToken(String token) {
        if (token == null || token.isBlank()) return;

        // Strip "Bearer " prefix nếu có
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token.isBlank()) return;

        // Không blacklist nếu đã tồn tại
        if (tokenBlacklistRepository.existsByTokenString(token)) return;

        try {
            LocalDateTime expiresAt = jwtUtil.extractExpiration(token)
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            String username = jwtUtil.extractUsername(token);
            User user = userRepository.findByUsername(username).orElse(null);

            TokenBlacklist blacklisted = TokenBlacklist.builder()
                    .tokenString(token)
                    .revokedAt(LocalDateTime.now())
                    .expiresAt(expiresAt)
                    .user(user)
                    .build();
            tokenBlacklistRepository.save(blacklisted);
            log.debug("[AUTH] Token revoked for user '{}'", username);
        } catch (Exception e) {
            log.warn("[AUTH] Could not revoke token: {}", e.getMessage());
        }
    }

    // ─── FR-04: ĐĂNG KÝ ───────────────────────────────────────────────────────
    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(RoleEnum.STUDENT)   // Đăng ký public luôn là STUDENT
                .isActive(true)
                .build();

        userRepository.save(user);
        log.info("[AUTH] New STUDENT registered: '{}'", request.getUsername());
    }

    // ─── FR-10: ĐỔI MẬT KHẨU ──────────────────────────────────────────────────
    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidStateException("New password and confirm password do not match.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidStateException("Current password is incorrect.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("[AUTH] Password changed for user '{}'", username);
    }

    // ─── FR-10: QUÊN MẬT KHẨU ─────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public void forgotPassword(String email) {
        // Kiểm tra email tồn tại
        userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + email));

        // Production: sinh reset token, lưu DB, gửi email
        // Demo: chỉ log
        log.info("[AUTH] Password reset requested for email '{}'", email);
    }

    // ─── FR-10: RESET MẬT KHẨU ────────────────────────────────────────────────
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidStateException("Passwords do not match.");
        }
        // Production: validate reset token từ DB/Redis
        log.info("[AUTH] Password reset completed");
    }

    // ─── HELPER ────────────────────────────────────────────────────────────────
    private AuthResponse buildAuthResponse(CustomUserDetails user,
                                           String accessToken, String refreshToken) {
        // Lấy role từ UserDetails — không query DB lần 2
        String roleStr = user.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");

        RoleEnum role;
        try {
            role = RoleEnum.valueOf(roleStr);
        } catch (Exception e) {
            role = null;
        }

        return AuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(role)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresIn(jwtUtil.getAccessTokenExpiration())
                .tokenType("Bearer")
                .build();
    }
}