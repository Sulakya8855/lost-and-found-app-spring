package com.crs.lost_and_found_app.entity;

import com.crs.lost_and_found_app.enums.ItemStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob // For potentially long descriptions
    private String description;

    private String category;

    private String locationFound; // Or locationLost

    private LocalDate dateReported; // Date when the item was lost or found

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status;

    @ManyToOne
    @JoinColumn(name = "reported_by_user_id", nullable = false)
    private User reportedBy;

    // Who currently has the item if it's FOUND (can be null)
    @ManyToOne
    @JoinColumn(name = "held_by_user_id")
    private User heldBy; 

    // Who claimed the item (can be null until status is CLAIMED)
    @ManyToOne
    @JoinColumn(name = "claimed_by_user_id")
    private User claimedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
} 