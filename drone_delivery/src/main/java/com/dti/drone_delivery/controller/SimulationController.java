package com.dti.drone_delivery.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dti.drone_delivery.service.SimulationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class SimulationController {
    
    private final SimulationService simulationService;
    
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startSimulation() {
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Simulação iniciada com sucesso");
        response.put("status", "RUNNING");
        response.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/generate-order")
    public ResponseEntity<Map<String, Object>> generateRandomOrder() {
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Pedido aleatório gerado");
        response.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSimulationStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "ACTIVE");
        status.put("lastUpdated", java.time.LocalDateTime.now());
        status.put("features", new String[] {
            "Geração automática de pedidos",
            "Atualização de estado de drones",
            "Simulação de bateria",
            "Alocação inteligente"
        });
        
        return ResponseEntity.ok(status);
    }
    
    @PostMapping("/reset")
public ResponseEntity<Map<String, Object>> resetSimulation() {
    simulationService.resetSimulation();
    
    Map<String, Object> response = new HashMap<>();
    response.put("message", "Simulação resetada com sucesso");
    response.put("timestamp", LocalDateTime.now());
    
    return ResponseEntity.ok(response);
}
}