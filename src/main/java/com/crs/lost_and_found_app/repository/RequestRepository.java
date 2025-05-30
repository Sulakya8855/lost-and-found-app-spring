package com.crs.lost_and_found_app.repository;

import com.crs.lost_and_found_app.entity.Request;
import com.crs.lost_and_found_app.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByRequesterId(Long userId);
    List<Request> findByItemId(Long itemId);
    List<Request> findByStatus(RequestStatus status);
} 