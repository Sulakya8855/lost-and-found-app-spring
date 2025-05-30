package com.crs.lost_and_found_app.dto;

import com.crs.lost_and_found_app.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestUpdateDto {
    private RequestStatus status;
    private String adminNotes;
} 