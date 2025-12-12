package com.dti.drone_delivery.controller;

import com.dti.drone_delivery.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/simulation")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class SimulationController {
    
    private final SimulationService simulationService;
    
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startSimulation() {
        simulationService.initializeSimulation();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Simulação iniciada com sucesso");
        response.put("status", "RUNNING");
        response.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/generate-order")
    public ResponseEntity<Map<String, Object>> generateRandomOrder() {
        simulationService.generateRandomOrders();
        
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
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Funcionalidade de reset será implementada");
        response.put("warning", "Isso limpará todos os dados");
        
        return ResponseEntity.ok(response);
    }
}