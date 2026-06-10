package com.java_web_service_qlkh_cdda.controller;

import com.java_web_service_qlkh_cdda.dto.request.UserRequest;
import com.java_web_service_qlkh_cdda.dto.response.ApiResponse;
import com.java_web_service_qlkh_cdda.dto.response.PageResponse;
import com.java_web_service_qlkh_cdda.dto.response.UserResponse;
import com.java_web_service_qlkh_cdda.enums.RoleEnum;
import com.java_web_service_qlkh_cdda.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - User Management", description = "CRUD users — Admin only")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users with search and pagination")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RoleEnum role) {
        PageResponse<UserResponse> result = userService.getAllUsers(page, size, keyword, role);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PostMapping
    @Operation(summary = "Create new user")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", userService.updateUser(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle user active/inactive status")
    public ResponseEntity<ApiResponse<Void>> toggleStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success("User status toggled", null));
    }
}