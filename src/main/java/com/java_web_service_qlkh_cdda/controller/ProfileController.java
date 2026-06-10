package com.java_web_service_qlkh_cdda.controller;

import com.java_web_service_qlkh_cdda.dto.request.ChangePasswordRequest;
import com.java_web_service_qlkh_cdda.dto.response.ApiResponse;
import com.java_web_service_qlkh_cdda.dto.response.UserResponse;
import com.java_web_service_qlkh_cdda.service.AuthService;
import com.java_web_service_qlkh_cdda.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Shared profile endpoints — all authenticated users")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping
    @Operation(summary = "Get my profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getCurrentUser(userDetails.getUsername())));
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change my password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }
}