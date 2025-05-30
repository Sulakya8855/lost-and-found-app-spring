package com.crs.lost_and_found_app.controller;

import com.crs.lost_and_found_app.dto.ItemRequestDto;
import com.crs.lost_and_found_app.dto.ItemResponseDto;
import com.crs.lost_and_found_app.enums.ItemStatus;
import com.crs.lost_and_found_app.service.ItemService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);
    private final ItemService itemService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'STAFF', 'ADMIN')")
    public ResponseEntity<?> createItem(@RequestBody ItemRequestDto itemRequestDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            logger.info("User: {} attempting to create item. Authorities: {}", authentication.getName(), authentication.getAuthorities().stream().map(Object::toString).collect(Collectors.joining(", ")));
        } else {
            logger.warn("No authentication found in security context for createItem.");
        }
        try {
            ItemResponseDto createdItem = itemService.createItem(itemRequestDto);
            return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
        } catch (SecurityException e) {
            logger.warn("Unauthorized item creation attempt (SecurityException from service): {}", e.getMessage());
            // If @PreAuthorize fails, this point might not be reached for a 403.
            // However, if the service layer throws a SecurityException, it could be caught here.
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating item: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred while creating the item.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(@PathVariable Long id) {
        try {
            ItemResponseDto item = itemService.getItemById(id);
            return ResponseEntity.ok(item);
        } catch (EntityNotFoundException e) {
            logger.warn("Get item by ID failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving item with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getAllItems() {
        List<ItemResponseDto> items = itemService.getAllItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ItemResponseDto>> getItemsByStatus(@PathVariable ItemStatus status) {
        List<ItemResponseDto> items = itemService.getItemsByStatus(status);
        return ResponseEntity.ok(items);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'STAFF', 'ADMIN')") // Further refined in service layer
    public ResponseEntity<?> updateItem(@PathVariable Long id, @RequestBody ItemRequestDto itemRequestDto) {
        try {
            ItemResponseDto updatedItem = itemService.updateItem(id, itemRequestDto);
            return ResponseEntity.ok(updatedItem);
        } catch (EntityNotFoundException e) {
            logger.warn("Update item failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            logger.warn("Unauthorized item update attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating item with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred while updating the item.");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Further refined in service layer
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        try {
            itemService.deleteItem(id);
            return ResponseEntity.ok("Item with ID " + id + " deleted successfully.");
        } catch (EntityNotFoundException e) {
            logger.warn("Delete item failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            logger.warn("Unauthorized item deletion attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting item with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred while deleting the item.");
        }
    }
} 