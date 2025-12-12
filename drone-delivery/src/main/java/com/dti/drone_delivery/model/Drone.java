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
@Table(name = "drones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Drone {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private Double maxWeight;
    
    @Column(nullable = false)
    private Double maxDistance;
    
    @Column(nullable = false)
    private Double batteryCapacity;
    
    private Double currentBattery;
    
    @Enumerated(EnumType.STRING)
    private DroneStatus status;
    
    private Double currentX;
    private Double currentY;
    
    @Builder.Default
    private Double baseX = 0.0;
    
    @Builder.Default
    private Double baseY = 0.0;
    
    private LocalDateTime lastUpdate;
    
    public enum DroneStatus {
        IDLE, LOADING, FLYING, DELIVERING, RETURNING, CHARGING, MAINTENANCE
    }
    
    @PrePersist
    protected void onCreate() {
        if (currentBattery == null) currentBattery = batteryCapacity;
        if (status == null) status = DroneStatus.IDLE;
        if (currentX == null) currentX = baseX;
        if (currentY == null) currentY = baseY;
        lastUpdate = LocalDateTime.now();
    }
}