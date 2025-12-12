package com.dti.drone_delivery.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String customerName;
    
    @Column(nullable = false)
    private Double locationX;
    
    @Column(nullable = false)
    private Double locationY;
    
    @Column(nullable = false)
    private Double weight;
    
    @Enumerated(EnumType.STRING)
    private Priority priority;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime scheduledFor;
    private LocalDateTime deliveredAt;
    
    private String assignedDroneId;
    
    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
    
    public enum OrderStatus {
        PENDING, ASSIGNED, IN_TRANSIT, DELIVERED, CANCELLED
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = OrderStatus.PENDING;
        if (priority == null) priority = Priority.MEDIUM;
    }
}