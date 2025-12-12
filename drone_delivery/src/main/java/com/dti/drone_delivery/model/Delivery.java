package com.dti.drone_delivery.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "deliveries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String orderId;
    
    @Column(nullable = false)
    private String droneId;
    
    private LocalDateTime startedAt;
    private LocalDateTime deliveredAt;
    private Double distanceTraveled;
    private Double batteryUsed;
    private Double totalTime;
    
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;
    
    public enum DeliveryStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, FAILED, CANCELLED
    }
    
    @PrePersist
    protected void onCreate() {
        if (startedAt == null) startedAt = LocalDateTime.now();
        if (status == null) status = DeliveryStatus.SCHEDULED;
    }
}