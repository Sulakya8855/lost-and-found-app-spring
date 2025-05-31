package com.crs.lost_and_found_app.service;

import com.crs.lost_and_found_app.dto.RequestCreateDto;
import com.crs.lost_and_found_app.dto.RequestResponseDto;
import com.crs.lost_and_found_app.dto.RequestUpdateDto;
import com.crs.lost_and_found_app.entity.Item;
import com.crs.lost_and_found_app.entity.Request;
import com.crs.lost_and_found_app.entity.User;
import com.crs.lost_and_found_app.enums.ItemStatus;
import com.crs.lost_and_found_app.enums.RequestStatus;
import com.crs.lost_and_found_app.repository.ItemRepository;
import com.crs.lost_and_found_app.repository.RequestRepository;
import com.crs.lost_and_found_app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {

    private static final Logger logger = LoggerFactory.getLogger(RequestService.class);
    private final RequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    public RequestResponseDto createRequest(RequestCreateDto requestCreateDto) {
        User currentUser = getCurrentAuthenticatedUser();
        Item item = itemRepository.findById(requestCreateDto.getItemId())
                .orElseThrow(() -> {
                    logger.warn("Item not found with ID: {} for claim request by User ID: {}", requestCreateDto.getItemId(), currentUser.getId());
                    return new EntityNotFoundException("Item not found with ID: " + requestCreateDto.getItemId());
                });

        if (item.getStatus() != ItemStatus.FOUND) {
            logger.warn("User ID: {} attempted to claim item ID: {} which is not in FOUND status (current status: {}).", currentUser.getId(), item.getId(), item.getStatus());
            throw new IllegalStateException("Item cannot be claimed as it is not currently in FOUND status.");
        }

        // Prevent user from claiming their own reported item if it was lost by them
        if (item.getReportedBy().getId().equals(currentUser.getId()) && item.getStatus() == ItemStatus.LOST) {
            // This logic might need refinement based on actual workflow for "lost" items later on
            logger.warn("User ID: {} attempted to claim their own reported LOST item ID: {}. This scenario might need review.", currentUser.getId(), item.getId());
            // For now, let's assume if it's FOUND, anyone can claim it. If it was LOST by someone, they shouldn't create a claim request for it.
        }

        // Check if user already has a PENDING request for this item
        boolean existingPendingRequest = requestRepository.findByItemId(item.getId()).stream()
                .anyMatch(req -> req.getRequester().getId().equals(currentUser.getId()) && req.getStatus() == RequestStatus.PENDING);
        if (existingPendingRequest) {
            logger.warn("User ID: {} already has a PENDING request for item ID: {}.", currentUser.getId(), item.getId());
            throw new IllegalStateException("You already have a pending request for this item.");
        }


        Request request = Request.builder()
                .item(item)
                .requester(currentUser)
                .status(RequestStatus.PENDING)
                .requestDate(LocalDateTime.now())
                .adminNotes("") // Initialize admin notes
                .build();

        Request savedRequest = requestRepository.save(request);
        logger.info("Claim request created successfully with ID: {} for Item ID: {} by User ID: {}", savedRequest.getId(), item.getId(), currentUser.getId());
        return mapToRequestResponseDto(savedRequest);
    }

    @Transactional
    public RequestResponseDto updateRequestStatus(Long requestId, RequestUpdateDto requestUpdateDto) {
        User adminOrStaffUser = getCurrentAuthenticatedUser(); // Ensure this user is ADMIN or STAFF via @PreAuthorize
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    logger.warn("Request not found with ID: {} for status update attempt by User ID: {}", requestId, adminOrStaffUser.getId());
                    return new EntityNotFoundException("Request not found with ID: " + requestId);
                });

        if (request.getStatus() != RequestStatus.PENDING) {
            logger.warn("Attempt to update non-pending request ID: {}. Current status: {}. Attempted by User ID: {}", requestId, request.getStatus(), adminOrStaffUser.getId());
            throw new IllegalStateException("Only PENDING requests can be updated.");
        }

        request.setStatus(requestUpdateDto.getStatus());
        request.setAdminNotes(requestUpdateDto.getAdminNotes());
        request.setResolutionDate(LocalDateTime.now());

        if (requestUpdateDto.getStatus() == RequestStatus.APPROVED) {
            Item item = request.getItem();
            item.setStatus(ItemStatus.CLAIMED);
            item.setClaimedBy(request.getRequester());
            item.setHeldBy(null); // No longer held by the finder/staff
            itemRepository.save(item);
            logger.info("Item ID: {} marked as CLAIMED for Request ID: {}. Processed by User ID: {}", item.getId(), requestId, adminOrStaffUser.getId());

            // Optionally, reject other PENDING requests for the same item
            rejectOtherPendingRequests(item.getId(), requestId);
        }

        Request updatedRequest = requestRepository.save(request);
        logger.info("Request ID: {} status updated to {} by User ID: {}", requestId, updatedRequest.getStatus(), adminOrStaffUser.getId());
        return mapToRequestResponseDto(updatedRequest);
    }

    private void rejectOtherPendingRequests(Long itemId, Long approvedRequestId) {
        List<Request> otherPendingRequests = requestRepository.findByItemId(itemId).stream()
                .filter(req -> req.getStatus() == RequestStatus.PENDING && !req.getId().equals(approvedRequestId))
                .collect(Collectors.toList());

        for (Request req : otherPendingRequests) {
            req.setStatus(RequestStatus.REJECTED);
            req.setAdminNotes("Item claimed by another user.");
            req.setResolutionDate(LocalDateTime.now());
            requestRepository.save(req);
            logger.info("Automatically rejected Request ID: {} for Item ID: {} as item was claimed.", req.getId(), itemId);
        }
    }

    @Transactional(readOnly = true)
    public RequestResponseDto getRequestById(Long id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Request not found with ID: {}", id);
                    return new EntityNotFoundException("Request not found with ID: " + id);
                });
        // Add authorization: only requester or admin/staff can view?
        return mapToRequestResponseDto(request);
    }

    @Transactional(readOnly = true)
    public List<RequestResponseDto> getAllRequests() {
        // Typically only for ADMIN/STAFF
        return requestRepository.findAll().stream()
                .map(this::mapToRequestResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RequestResponseDto> getRequestsByUserId(Long userId) {
        // User can see their own requests, or ADMIN/STAFF can see for a user
        if (!getCurrentAuthenticatedUser().getId().equals(userId) && !isAdminOrStaff(getCurrentAuthenticatedUser())) {
             throw new SecurityException("You are not authorized to view requests for this user.");
        }
        return requestRepository.findByRequesterId(userId).stream()
                .map(this::mapToRequestResponseDto)
                .collect(Collectors.toList());
    }

     @Transactional(readOnly = true)
    public List<RequestResponseDto> getRequestsByItemId(Long itemId) {
        // Staff/Admin might want to see all requests for an item.
        return requestRepository.findByItemId(itemId).stream()
                .map(this::mapToRequestResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RequestResponseDto> getRequestsByStatus(RequestStatus status) {
        // Typically for ADMIN/STAFF to filter requests
        return requestRepository.findByStatus(status).stream()
                .map(this::mapToRequestResponseDto)
                .collect(Collectors.toList());
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Attempt to access service method without authentication.");
            throw new SecurityException("User not authenticated");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Authenticated user {} not found in database.", username);
                    return new EntityNotFoundException("Authenticated user not found: " + username);
                });
    }

    private boolean isAdminOrStaff(User user) {
        return user.getRole() == com.crs.lost_and_found_app.enums.UserRole.ADMIN || 
               user.getRole() == com.crs.lost_and_found_app.enums.UserRole.STAFF;
    }

    private RequestResponseDto mapToRequestResponseDto(Request request) {
        return RequestResponseDto.builder()
                .id(request.getId())
                .itemId(request.getItem().getId())
                .itemName(request.getItem().getName())
                .requesterId(request.getRequester().getId())
                .requesterUsername(request.getRequester().getUsername())
                .status(request.getStatus())
                .message(request.getMessage()) // Assuming message is part of Request entity
                .requestDate(request.getRequestDate())
                .resolutionDate(request.getResolutionDate())
                .adminNotes(request.getAdminNotes())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

    @Transactional
    public void deleteRequest(Long requestId) {
        User currentUser = getCurrentAuthenticatedUser();
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    logger.warn("Request not found with ID: {} for delete attempt by User ID: {}", requestId, currentUser.getId());
                    return new EntityNotFoundException("Request not found with ID: " + requestId);
                });

        // Check if the user is the requester or an admin/staff
        if (!request.getRequester().getId().equals(currentUser.getId()) && !isAdminOrStaff(currentUser)) {
            logger.warn("User ID: {} attempted to delete request ID: {} owned by User ID: {}. Unauthorized.",
                    currentUser.getId(), requestId, request.getRequester().getId());
            throw new SecurityException("You are not authorized to delete this request.");
        }

        // Prevent deletion of approved requests where the item is already claimed, as this could lead to inconsistencies.
        // Other statuses (PENDING, REJECTED, CANCELLED) should be deletable.
        if (request.getStatus() == RequestStatus.APPROVED && request.getItem().getStatus() == ItemStatus.CLAIMED) {
            logger.warn("Attempt to delete an APPROVED request (ID: {}) where the item (ID: {}) is already CLAIMED. User ID: {}. Operation denied.",
                    requestId, request.getItem().getId(), currentUser.getId());
            throw new IllegalStateException("Cannot delete a request that has been approved and the item claimed. Please reject or cancel if necessary.");
        }

        requestRepository.delete(request);
        logger.info("Request with ID: {} successfully deleted by User ID: {}. Request was made by User ID: {}",
                requestId, currentUser.getId(), request.getRequester().getId());
    }
}