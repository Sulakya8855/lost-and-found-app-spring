package com.crs.lost_and_found_app.repository;

import com.crs.lost_and_found_app.entity.Item;
import com.crs.lost_and_found_app.enums.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByStatus(ItemStatus status);
    List<Item> findByCategory(String category);
    List<Item> findByReportedById(Long userId);
} 