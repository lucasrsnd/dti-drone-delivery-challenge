package com.dti.drone_delivery.dto;

import com.dti.drone_delivery.model.Order;

import lombok.Data;

@Data
public class OrderRequest {
    private String customerName;
    private Double locationX;
    private Double locationY;
    private Double weight;
    private Order.Priority priority;
    
    public Order toEntity() {
        return Order.builder()
            .customerName(customerName)
            .locationX(locationX)
            .locationY(locationY)
            .weight(weight)
            .priority(priority)
            .build();
    }
}