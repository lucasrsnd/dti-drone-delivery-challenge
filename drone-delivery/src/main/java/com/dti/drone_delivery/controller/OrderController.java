package com.dti.drone_delivery.controller;

import com.dti.drone_delivery.dto.OrderRequest;
import com.dti.drone_delivery.model.Order;
import com.dti.drone_delivery.service.OrderService;
import com.dti.drone_delivery.service.AllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class OrderController {
    
    private final OrderService orderService;
    private final AllocationService allocationService;
    
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
    
    @GetMapping("/pending")
    public ResponseEntity<List<Order>> getPendingOrders() {
        return ResponseEntity.ok(orderService.getPendingOrders());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable String id) {
        return orderService.getAllOrders().stream()
            .filter(order -> order.getId().equals(id))
            .findFirst()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }
    
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> createBatchOrders(@RequestBody List<OrderRequest> requests) {
        List<Order> createdOrders = new java.util.ArrayList<>();
        for (OrderRequest request : requests) {
            createdOrders.add(orderService.createOrder(request));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Pedidos criados com sucesso");
        response.put("count", createdOrders.size());
        response.put("orders", createdOrders);
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable String id,
            @RequestParam Order.OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelOrder(@PathVariable String id) {
        orderService.cancelOrder(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Pedido cancelado com sucesso");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getOrderMetrics() {
        long pending = orderService.countPendingOrders();
        List<Order> allOrders = orderService.getAllOrders();
        long delivered = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .count();
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalOrders", allOrders.size());
        metrics.put("pendingOrders", pending);
        metrics.put("deliveredOrders", delivered);
        metrics.put("deliveryRate", allOrders.isEmpty() ? 0 : (double) delivered / allOrders.size());
        metrics.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(metrics);
    }
    
    @PostMapping("/allocate")
    public ResponseEntity<Map<String, Object>> allocateOrders() {
        Map<String, List<Order>> allocation = allocationService.allocateOrdersToDrones();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Alocação realizada com sucesso");
        response.put("allocationCount", allocation.size());
        
        Map<String, Integer> droneAllocations = new HashMap<>();
        for (Map.Entry<String, List<Order>> entry : allocation.entrySet()) {
            droneAllocations.put(entry.getKey(), entry.getValue().size());
        }
        response.put("allocations", droneAllocations);
        
        return ResponseEntity.ok(response);
    }
}