package com.java_web_service_qlkh_cdda.service;

import com.java_web_service_qlkh_cdda.dto.request.UserRequest;
import com.java_web_service_qlkh_cdda.dto.response.PageResponse;
import com.java_web_service_qlkh_cdda.dto.response.UserResponse;
import com.java_web_service_qlkh_cdda.enums.RoleEnum;

public interface UserService {
    UserResponse createUser(UserRequest request);
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserRequest request);
    void deleteUser(Long id);
    void toggleUserStatus(Long id);
    PageResponse<UserResponse> getAllUsers(int page, int size, String keyword, RoleEnum role);
    UserResponse getCurrentUser(String username);
}