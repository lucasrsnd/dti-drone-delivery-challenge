package com.dti.drone_delivery.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dti.drone_delivery.dto.OrderRequest;
import com.dti.drone_delivery.model.Order;
import com.dti.drone_delivery.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    
    public Order createOrder(OrderRequest request) {
        Order order = request.toEntity();
        return orderRepository.save(order);
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus(Order.OrderStatus.PENDING);
    }
    
    public Order updateOrderStatus(String id, Order.OrderStatus status) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        order.setStatus(status);
        
        if (status == Order.OrderStatus.DELIVERED) {
            order.setDeliveredAt(java.time.LocalDateTime.now());
        }
        
        return orderRepository.save(order);
    }
    
    public void cancelOrder(String id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
    
    public long countPendingOrders() {
        return orderRepository.countByStatus(Order.OrderStatus.PENDING);
    }
    
    public List<Order> getOrdersByDrone(String droneId) {
        return orderRepository.findByAssignedDroneId(droneId);
    }
}