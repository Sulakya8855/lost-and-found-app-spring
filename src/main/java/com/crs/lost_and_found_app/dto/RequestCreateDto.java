package com.crs.lost_and_found_app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestCreateDto {
    private Long itemId;
    private String message; // Optional message from the requester
} 