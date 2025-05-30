package com.crs.lost_and_found_app.dto;

import com.crs.lost_and_found_app.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestResponseDto {
    private Long id;
    private Long itemId;
    private String itemName; // For convenience
    private Long requesterId;
    private String requesterUsername;
    private RequestStatus status;
    private String message; // Requester's message
    private LocalDateTime requestDate;
    private LocalDateTime resolutionDate;
    private String adminNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 