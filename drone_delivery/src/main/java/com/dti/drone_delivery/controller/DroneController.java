package com.dti.drone_delivery.controller;

import com.dti.drone_delivery.dto.DroneResponse;
import com.dti.drone_delivery.model.Drone;
import com.dti.drone_delivery.service.DroneService;
import com.dti.drone_delivery.service.AllocationService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dti.drone_delivery.model.Order;

@RestController
@RequestMapping("/api/drones")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class DroneController {
    
    private final DroneService droneService;
    private final AllocationService allocationService;
    
    @GetMapping
    public ResponseEntity<List<DroneResponse>> getAllDrones() {
        return ResponseEntity.ok(droneService.getAllDrones());
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<DroneResponse>> getAvailableDrones(
            @RequestParam(defaultValue = "20.0") Double minBattery) {
        return ResponseEntity.ok(droneService.getAvailableDrones(minBattery));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DroneResponse> getDroneById(@PathVariable String id) {
        return ResponseEntity.ok(droneService.getDroneById(id));
    }
    
    @PostMapping
    public ResponseEntity<Drone> createDrone(@RequestBody Drone drone) {
        return ResponseEntity.ok(droneService.createDrone(drone));
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Drone> updateDroneStatus(
            @PathVariable String id,
            @RequestParam Drone.DroneStatus status) {
        return ResponseEntity.ok(droneService.updateDroneStatus(id, status));
    }
    
    @PostMapping("/{id}/recharge")
    public ResponseEntity<Void> rechargeDrone(@PathVariable String id) {
        droneService.rechargeDrone(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/allocate")
public ResponseEntity<Map<String, List<Map<String, Object>>>> allocateOrders() {
    Map<String, List<com.dti.drone_delivery.model.Order>> allocation = 
        allocationService.allocateOrdersToDrones();

    Map<String, List<Map<String, Object>>> response = new HashMap<>();
    
    for (Map.Entry<String, List<Order>> entry : allocation.entrySet()) {
        String droneId = entry.getKey();
        List<Order> orders = entry.getValue();
        
        List<Map<String, Object>> orderList = new ArrayList<>();
        for (Order order : orders) {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.getId());
            orderMap.put("customer", order.getCustomerName());
            orderMap.put("weight", order.getWeight());
            orderMap.put("priority", order.getPriority().toString());
            orderList.add(orderMap);
        }
        response.put(droneId, orderList);
    }
    
    return ResponseEntity.ok(response);
}
    
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getDroneMetrics() {
        long idle = droneService.countByStatus(Drone.DroneStatus.IDLE);
        long flying = droneService.countByStatus(Drone.DroneStatus.FLYING);
        long charging = droneService.countByStatus(Drone.DroneStatus.CHARGING);
        
        Map<String, Object> metrics = Map.of(
            "totalDrones", droneService.getAllDrones().size(),
            "idleDrones", idle,
            "activeDrones", flying,
            "chargingDrones", charging,
            "timestamp", java.time.LocalDateTime.now()
        );
        
        return ResponseEntity.ok(metrics);
    }
}