package com.crs.lost_and_found_app.dto;

import com.crs.lost_and_found_app.enums.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {
    private String name;
    private String description;
    private String category;
    private String locationFound; // Or locationLost, depending on the context
    private LocalDate dateReported;
    private ItemStatus status; // Initial status when reporting
} 