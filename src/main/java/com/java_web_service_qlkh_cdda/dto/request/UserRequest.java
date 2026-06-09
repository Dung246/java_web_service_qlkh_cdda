package com.java_web_service_qlkh_cdda.dto.request;

import com.java_web_service_qlkh_cdda.enums.RoleEnum;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 50)
    private String username;

    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String fullName;

    @NotNull(message = "Role is required")
    private RoleEnum role;

    private Boolean isActive = true;
}