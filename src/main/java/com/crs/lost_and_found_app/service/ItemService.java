package com.crs.lost_and_found_app.service;

import com.crs.lost_and_found_app.dto.ItemRequestDto;
import com.crs.lost_and_found_app.dto.ItemResponseDto;
import com.crs.lost_and_found_app.entity.Item;
import com.crs.lost_and_found_app.entity.User;
import com.crs.lost_and_found_app.enums.ItemStatus;
import com.crs.lost_and_found_app.repository.ItemRepository;
import com.crs.lost_and_found_app.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    public ItemResponseDto createItem(ItemRequestDto itemRequestDto) {
        User currentUser = getCurrentAuthenticatedUser();

        Item item = Item.builder()
                .name(itemRequestDto.getName())
                .description(itemRequestDto.getDescription())
                .category(itemRequestDto.getCategory())
                .locationFound(itemRequestDto.getLocationFound())
                .dateReported(itemRequestDto.getDateReported())
                .status(itemRequestDto.getStatus()) // Should be LOST or FOUND initially
                .reportedBy(currentUser)
                .build();

        if (item.getStatus() == ItemStatus.FOUND) {
            item.setHeldBy(currentUser); // If a user reports a found item, they are initially holding it.
        }

        Item savedItem = itemRepository.save(item);
        logger.info("Item created successfully with ID: {} by User ID: {}", savedItem.getId(), currentUser.getId());
        return mapToItemResponseDto(savedItem);
    }

    @Transactional(readOnly = true)
    public ItemResponseDto getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Item not found with ID: {}", id);
                    return new EntityNotFoundException("Item not found with ID: " + id);
                });
        return mapToItemResponseDto(item);
    }

    @Transactional(readOnly = true)
    public List<ItemResponseDto> getAllItems() {
        return itemRepository.findAll().stream()
                .map(this::mapToItemResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ItemResponseDto> getItemsByStatus(ItemStatus status) {
        return itemRepository.findByStatus(status).stream()
                .map(this::mapToItemResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ItemResponseDto updateItem(Long id, ItemRequestDto itemRequestDto) {
        User currentUser = getCurrentAuthenticatedUser();
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Item not found with ID: {} for update attempt by User ID: {}", id, currentUser.getId());
                    return new EntityNotFoundException("Item not found with ID: " + id);
                });

        // Basic authorization: Only the user who reported the item or an ADMIN/STAFF can update it (for now).
        // More granular control might be needed, e.g., STAFF can update any FOUND item.
        if (!item.getReportedBy().getId().equals(currentUser.getId()) &&
            !currentUser.getRole().name().equals("ADMIN") &&
            !currentUser.getRole().name().equals("STAFF")) {
            logger.warn("User ID: {} attempted to update Item ID: {} without permission.", currentUser.getId(), id);
            throw new SecurityException("You are not authorized to update this item.");
        }

        item.setName(itemRequestDto.getName());
        item.setDescription(itemRequestDto.getDescription());
        item.setCategory(itemRequestDto.getCategory());
        item.setLocationFound(itemRequestDto.getLocationFound());
        item.setDateReported(itemRequestDto.getDateReported());
        // Status updates might need more complex logic, e.g., if an item is CLAIMED.
        // For now, allow direct status update via DTO, but this should be refined.
        // For example, only ADMIN/STAFF can mark an item as CLAIMED after a request is APPROVED.
        item.setStatus(itemRequestDto.getStatus());

        Item updatedItem = itemRepository.save(item);
        logger.info("Item with ID: {} updated successfully by User ID: {}", updatedItem.getId(), currentUser.getId());
        return mapToItemResponseDto(updatedItem);
    }

    @Transactional
    public void deleteItem(Long id) {
        User currentUser = getCurrentAuthenticatedUser();
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Item not found with ID: {} for delete attempt by User ID: {}", id, currentUser.getId());
                    return new EntityNotFoundException("Item not found with ID: " + id);
                });

        // Basic authorization: Only the user who reported it or an ADMIN can delete.
        if (!item.getReportedBy().getId().equals(currentUser.getId()) &&
            !currentUser.getRole().name().equals("ADMIN")) {
            logger.warn("User ID: {} attempted to delete Item ID: {} without permission.", currentUser.getId(), id);
            throw new SecurityException("You are not authorized to delete this item.");
        }

        // Add logic here to handle related entities, e.g., associated Requests, if necessary before deleting.
        itemRepository.delete(item);
        logger.info("Item with ID: {} deleted successfully by User ID: {}", id, currentUser.getId());
    }

    // Helper method to get the current authenticated user
    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("User not authenticated");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Authenticated user not found in database: {}", username);
                    return new EntityNotFoundException("Authenticated user not found: " + username);
                });
    }

    // Helper method to map Item entity to ItemResponseDto
    private ItemResponseDto mapToItemResponseDto(Item item) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .category(item.getCategory())
                .locationFound(item.getLocationFound())
                .dateReported(item.getDateReported())
                .status(item.getStatus())
                .reportedById(item.getReportedBy().getId())
                .reportedByUsername(item.getReportedBy().getUsername())
                .heldById(item.getHeldBy() != null ? item.getHeldBy().getId() : null)
                .heldByUsername(item.getHeldBy() != null ? item.getHeldBy().getUsername() : null)
                .claimedById(item.getClaimedBy() != null ? item.getClaimedBy().getId() : null)
                .claimedByUsername(item.getClaimedBy() != null ? item.getClaimedBy().getUsername() : null)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
} 