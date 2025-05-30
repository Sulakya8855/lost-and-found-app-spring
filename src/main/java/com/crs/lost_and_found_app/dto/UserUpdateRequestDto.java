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
public class UserUpdateRequestDto {
    private UserRole role;
    // Add other fields an admin might update, e.g., account status (isEnabled, isLocked)
    // For now, just role change.
} 