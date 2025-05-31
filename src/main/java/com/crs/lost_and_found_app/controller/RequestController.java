package com.crs.lost_and_found_app.controller;

import com.crs.lost_and_found_app.dto.RequestCreateDto;
import com.crs.lost_and_found_app.dto.RequestResponseDto;
import com.crs.lost_and_found_app.dto.RequestUpdateDto;
import com.crs.lost_and_found_app.enums.RequestStatus;
import com.crs.lost_and_found_app.service.RequestService;
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
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
public class RequestController {

    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);
    private final RequestService requestService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'STAFF', 'ADMIN')")
    public ResponseEntity<?> createRequest(@RequestBody RequestCreateDto requestCreateDto) {
        try {
            RequestResponseDto createdRequest = requestService.createRequest(requestCreateDto);
            return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
        } catch (EntityNotFoundException | IllegalStateException e) {
            logger.warn("Failed to create request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SecurityException e) {
            logger.warn("Unauthorized request creation attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred while creating the request.");
        }
    }

    @PutMapping("/{requestId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<?> updateRequestStatus(@PathVariable Long requestId, @RequestBody RequestUpdateDto requestUpdateDto) {
        try {
            RequestResponseDto updatedRequest = requestService.updateRequestStatus(requestId, requestUpdateDto);
            return ResponseEntity.ok(updatedRequest);
        } catch (EntityNotFoundException | IllegalStateException e) {
            logger.warn("Failed to update request status for ID {}: {}", requestId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SecurityException e) {
            logger.warn("Unauthorized request status update attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating request status for ID {}: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'STAFF', 'ADMIN')") // Further refined in service
    public ResponseEntity<?> getRequestById(@PathVariable Long id) {
        try {
            RequestResponseDto request = requestService.getRequestById(id);
            // Service layer should handle if the current user is allowed to see this specific request
            return ResponseEntity.ok(request);
        } catch (EntityNotFoundException e) {
            logger.warn("Get request by ID failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            logger.warn("Unauthorized attempt to get request by ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
         catch (Exception e) {
            logger.error("Error retrieving request with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<RequestResponseDto>> getAllRequests() {
        List<RequestResponseDto> requests = requestService.getAllRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'STAFF', 'ADMIN')") // Further refined in service
    public ResponseEntity<?> getRequestsByUserId(@PathVariable Long userId) {
         try {
            List<RequestResponseDto> requests = requestService.getRequestsByUserId(userId);
            return ResponseEntity.ok(requests);
        } catch (SecurityException e) {
            logger.warn("Unauthorized attempt to get requests for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving requests for user ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @GetMapping("/item/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<RequestResponseDto>> getRequestsByItemId(@PathVariable Long itemId) {
        List<RequestResponseDto> requests = requestService.getRequestsByItemId(itemId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<RequestResponseDto>> getRequestsByStatus(@PathVariable RequestStatus status) {
        List<RequestResponseDto> requests = requestService.getRequestsByStatus(status);
        return ResponseEntity.ok(requests);
    }

    @DeleteMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('USER', 'STAFF', 'ADMIN')")
    public ResponseEntity<?> deleteRequest(@PathVariable Long requestId) {
        try {
            requestService.deleteRequest(requestId);
            logger.info("Request with ID {} successfully deleted", requestId);
            return ResponseEntity.ok("Request deleted successfully");
        } catch (EntityNotFoundException e) {
            logger.warn("Failed to delete request with ID {}: {}", requestId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            logger.warn("Cannot delete request with ID {}: {}", requestId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SecurityException e) {
            logger.warn("Unauthorized attempt to delete request with ID {}: {}", requestId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting request with ID {}: {}", requestId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred while deleting the request.");
        }
    }

    // Note: Deleting requests might not be a standard user feature.
    // If needed, it should likely be restricted to ADMINs and handle cascading effects.
    // For now, no DELETE endpoint for Requests is implemented based on common flows.
} 