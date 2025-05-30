package com.crs.lost_and_found_app.service;

import com.crs.lost_and_found_app.dto.UserResponseDto;
import com.crs.lost_and_found_app.dto.UserUpdateRequestDto;
import com.crs.lost_and_found_app.entity.User;
import com.crs.lost_and_found_app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    // We might need PasswordEncoder if we allow admins to reset passwords, but not for just role changes.

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        // This is an admin-only operation, to be enforced by @PreAuthorize in controller
        return userRepository.findAll().stream()
                .map(this::mapToUserResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long userId) {
        // Admin-only operation
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {} for admin retrieval.", userId);
                    return new EntityNotFoundException("User not found with ID: " + userId);
                });
        return mapToUserResponseDto(user);
    }

    @Transactional
    public UserResponseDto updateUserRole(Long userId, UserUpdateRequestDto userUpdateRequestDto) {
        // Admin-only operation
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {} for role update.", userId);
                    return new EntityNotFoundException("User not found with ID: " + userId);
                });

        // Prevent admin from accidentally changing their own role to something lower if they are the only admin?
        // This kind of business rule can be added if necessary.

        user.setRole(userUpdateRequestDto.getRole());
        User updatedUser = userRepository.save(user);
        logger.info("User ID: {} role updated to {} by an admin.", userId, updatedUser.getRole());
        return mapToUserResponseDto(updatedUser);
    }

    // Deleting users can be complex due to relationships (items reported, requests made).
    // A soft delete (marking as inactive) is often preferred.
    // For now, a hard delete is implemented but should be used with caution.
    @Transactional
    public void deleteUser(Long userId) {
        // Admin-only operation
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {} for deletion.", userId);
                    return new EntityNotFoundException("User not found with ID: " + userId);
                });

        // Add checks here: e.g., don't allow an admin to delete themselves if they are the last admin.
        // Add logic to handle/reassign/anonymize related Items and Requests before deleting a user.
        // This is a placeholder and needs careful consideration in a real app.
        logger.warn("Attempting to hard delete User ID: {}. Ensure cascading effects are handled (Items, Requests).", userId);
        
        // Before deleting user, you might want to nullify their references in Items and Requests
        // or reassign them to a generic "deleted_user" if your schema supports it to maintain data integrity.
        // This is a simplified example.
        // itemRepository.findByReportedById(userId).forEach(item -> item.setReportedBy(null));
        // itemRepository.findByClaimedById(userId).forEach(item -> item.setClaimedBy(null));
        // itemRepository.findByHeldById(userId).forEach(item -> item.setHeldBy(null));
        // requestRepository.findByRequesterId(userId).forEach(request -> request.setRequester(null));

        userRepository.delete(user);
        logger.info("User ID: {} deleted successfully by an admin.", userId);
    }


    private UserResponseDto mapToUserResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
} 