package com.dti.drone_delivery.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "obstacles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Obstacle {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String name;
    
    @Column(nullable = false)
    private Double centerX;
    
    @Column(nullable = false)
    private Double centerY;
    
    @Column(nullable = false)
    private Double radius;
    
    private String type;

    public boolean isPointInside(Double x, Double y) {
        double distance = Math.sqrt(
            Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2)
        );
        return distance <= radius;
    }
}