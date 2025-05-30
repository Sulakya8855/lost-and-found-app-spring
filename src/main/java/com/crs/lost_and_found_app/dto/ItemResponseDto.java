package com.crs.lost_and_found_app.dto;

import com.crs.lost_and_found_app.enums.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponseDto {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String locationFound;
    private LocalDate dateReported;
    private ItemStatus status;
    private Long reportedById;
    private String reportedByUsername;
    private Long heldById; // User ID of who is holding the item if FOUND
    private String heldByUsername;
    private Long claimedById; // User ID of who claimed the item if CLAIMED
    private String claimedByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 