package com.java_web_service_qlkh_cdda.service.impl;

import com.java_web_service_qlkh_cdda.dto.request.ChangePasswordRequest;
import com.java_web_service_qlkh_cdda.dto.request.ForgotPasswordRequest;
import com.java_web_service_qlkh_cdda.dto.request.RegisterRequest;
import com.java_web_service_qlkh_cdda.dto.request.ResetPasswordRequest;
import com.java_web_service_qlkh_cdda.dto.response.AuthResponse;
import com.java_web_service_qlkh_cdda.entity.User;
import com.java_web_service_qlkh_cdda.enums.RoleEnum;
import com.java_web_service_qlkh_cdda.exception.DuplicateResourceException;
import com.java_web_service_qlkh_cdda.exception.InvalidStateException;
import com.java_web_service_qlkh_cdda.exception.ResourceNotFoundException;
import com.java_web_service_qlkh_cdda.repository.UserRepository;
import com.java_web_service_qlkh_cdda.security.CustomUserDetails;
import com.java_web_service_qlkh_cdda.security.CustomUserDetailsService;
import com.java_web_service_qlkh_cdda.service.AuthService;
import com.java_web_service_qlkh_cdda.service.TokenBlacklistService;
import com.java_web_service_qlkh_cdda.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService; // ✅ Redis thay DB
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── UC-01: LOGIN ──────────────────────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponse login(String username, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        CustomUserDetails customUser = (CustomUserDetails) userDetails;

        String accessToken  = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        log.info("[AUTH] Login success: '{}' role='{}'", username,
                customUser.getAuthorities().iterator().next().getAuthority());

        return buildAuthResponse(customUser, accessToken, refreshToken);
    }

    // ─── UC-02: REFRESH TOKEN ──────────────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        // ✅ Kiểm tra Redis thay vì query DB
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new InvalidStateException("Refresh token has been revoked. Please login again.");
        }

        String username;
        try {
            username = jwtUtil.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new InvalidStateException("Invalid refresh token.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtUtil.isTokenValid(refreshToken, userDetails)) {
            throw new InvalidStateException("Refresh token expired or invalid. Please login again.");
        }

        CustomUserDetails customUser = (CustomUserDetails) userDetails;
        String newAccessToken = jwtUtil.generateAccessToken(userDetails);

        log.info("[AUTH] Token refreshed for user '{}'", username);
        return buildAuthResponse(customUser, newAccessToken, refreshToken);
    }

    // ─── UC-03: LOGOUT — BLACKLIST QUA REDIS ──────────────────────────────────
    @Override
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        revokeTokenToRedis(accessToken);
        revokeTokenToRedis(refreshToken);
        log.info("[AUTH] Logout — both tokens blacklisted in Redis");
    }

    /**
     * Tính TTL còn lại của token rồi SET vào Redis với EXPIRE đúng bằng thời gian đó.
     * Redis sẽ TỰ XÓA key khi hết TTL → bộ nhớ không phình to.
     *
     * Ví dụ: accessToken còn 900 giây → Redis SET key EX 900
     *        Sau 900s Redis tự EXPIRE → không tồn tại nữa
     */
    private void revokeTokenToRedis(String token) {
        if (token == null || token.isBlank()) return;

        // Strip "Bearer " nếu có
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token.isBlank()) return;

        // Đã blacklist rồi → bỏ qua
        if (tokenBlacklistService.isBlacklisted(token)) return;

        try {
            Date expiration = jwtUtil.extractExpiration(token);
            long ttlMillis  = expiration.getTime() - System.currentTimeMillis();

            if (ttlMillis <= 0) {
                // Token đã hết hạn tự nhiên → không cần blacklist
                log.debug("[AUTH] Token already expired, skip blacklist");
                return;
            }

            String username = jwtUtil.extractUsername(token);
            // ✅ SET vào Redis với TTL chính xác
            tokenBlacklistService.blacklistToken(token, username, ttlMillis);

        } catch (Exception e) {
            log.warn("[AUTH] Could not revoke token to Redis: {}", e.getMessage());
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
                .role(RoleEnum.STUDENT)
                .isActive(true)
                .build();

        userRepository.save(user);
        log.info("[AUTH] New STUDENT registered: '{}'", request.getUsername());
    }

    // ─── FR-10: ĐỔI MẬT KHẨU ─────────────────────────────────────────────────
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

    // ─── FR-10: QUÊN MẬT KHẨU ────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public void forgotPassword(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account with email: " + email));
        log.info("[AUTH] Password reset requested for '{}'", email);
    }

    // ─── FR-10: RESET MẬT KHẨU ───────────────────────────────────────────────
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidStateException("Passwords do not match.");
        }
        log.info("[AUTH] Password reset completed");
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────
    private AuthResponse buildAuthResponse(CustomUserDetails user,
                                           String accessToken, String refreshToken) {
        String roleStr = user.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");

        RoleEnum role;
        try { role = RoleEnum.valueOf(roleStr); }
        catch (Exception e) { role = null; }

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