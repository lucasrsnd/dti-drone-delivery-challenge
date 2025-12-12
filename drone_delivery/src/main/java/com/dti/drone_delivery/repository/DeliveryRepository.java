package com.dti.drone_delivery.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dti.drone_delivery.model.Delivery;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, String> {
    
    List<Delivery> findByDroneId(String droneId);
    
    List<Delivery> findByStatus(Delivery.DeliveryStatus status);
    
    @Query("SELECT d FROM Delivery d WHERE d.deliveredAt BETWEEN :start AND :end")
    List<Delivery> findDeliveriesBetween(LocalDateTime start, LocalDateTime end);
    
    List<Delivery> findByDroneIdAndStatus(String droneId, Delivery.DeliveryStatus status);
}