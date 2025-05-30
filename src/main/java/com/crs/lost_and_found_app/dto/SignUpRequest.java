package com.crs.lost_and_found_app.dto;

import com.crs.lost_and_found_app.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    private String username;
    private String email;
    private String password;
    private UserRole role; // Or you might want to control role assignment differently
} 