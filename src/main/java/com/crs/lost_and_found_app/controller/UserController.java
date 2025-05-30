package com.crs.lost_and_found_app.controller;

import com.crs.lost_and_found_app.dto.UserResponseDto;
import com.crs.lost_and_found_app.dto.UserUpdateRequestDto;
import com.crs.lost_and_found_app.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Secure all methods in this controller for ADMIN only
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            UserResponseDto user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (EntityNotFoundException e) {
            logger.warn("Admin attempt to get non-existent user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving user with ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long userId, @RequestBody UserUpdateRequestDto userUpdateRequestDto) {
        try {
            UserResponseDto updatedUser = userService.updateUserRole(userId, userUpdateRequestDto);
            return ResponseEntity.ok(updatedUser);
        } catch (EntityNotFoundException e) {
            logger.warn("Admin attempt to update role for non-existent user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Catch more specific exceptions if needed, e.g., for business rule violations
            logger.error("Error updating role for user ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred while updating user role.");
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        // Ensure the admin is not deleting themselves, or handle consequences carefully.
        // This is a basic implementation.
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok("User with ID " + userId + " deleted successfully.");
        } catch (EntityNotFoundException e) {
            logger.warn("Admin attempt to delete non-existent user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Catch more specific exceptions, e.g., if deletion is not allowed due to foreign key constraints not handled
            logger.error("Error deleting user with ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred while deleting user.");
        }
    }
} 