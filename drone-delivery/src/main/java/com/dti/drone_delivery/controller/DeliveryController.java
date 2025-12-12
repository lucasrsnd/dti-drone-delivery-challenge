package com.dti.drone_delivery.controller;

import com.dti.drone_delivery.model.Delivery;
import com.dti.drone_delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class DeliveryController {
    
    private final DeliveryService deliveryService;
    
    @GetMapping
    public ResponseEntity<List<Delivery>> getAllDeliveries() {
        return ResponseEntity.ok(deliveryService.getAllDeliveries());
    }
    
    @GetMapping("/drone/{droneId}")
    public ResponseEntity<List<Delivery>> getDeliveriesByDrone(@PathVariable String droneId) {
        return ResponseEntity.ok(deliveryService.getDeliveriesByDrone(droneId));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Delivery>> getDeliveriesByStatus(@PathVariable Delivery.DeliveryStatus status) {
        return ResponseEntity.ok(deliveryService.getDeliveriesByStatus(status));
    }
    
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getDeliveryMetrics() {
        List<Delivery> allDeliveries = deliveryService.getAllDeliveries();
        
        long completed = allDeliveries.stream()
            .filter(d -> d.getStatus() == Delivery.DeliveryStatus.COMPLETED)
            .count();
        
        double totalDistance = allDeliveries.stream()
            .filter(d -> d.getDistanceTraveled() != null)
            .mapToDouble(Delivery::getDistanceTraveled)
            .sum();
        
        double avgDeliveryTime = allDeliveries.stream()
            .filter(d -> d.getTotalTime() != null && d.getStatus() == Delivery.DeliveryStatus.COMPLETED)
            .mapToDouble(Delivery::getTotalTime)
            .average()
            .orElse(0.0);
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalDeliveries", allDeliveries.size());
        metrics.put("completedDeliveries", completed);
        metrics.put("successRate", allDeliveries.isEmpty() ? 0 : (double) completed / allDeliveries.size());
        metrics.put("totalDistanceKm", totalDistance);
        metrics.put("averageDeliveryTimeMinutes", avgDeliveryTime);
        metrics.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/report")
    public ResponseEntity<Map<String, Object>> generateDeliveryReport() {
        Map<String, Object> report = deliveryService.generateDeliveryReport();
        return ResponseEntity.ok(report);
    }
}