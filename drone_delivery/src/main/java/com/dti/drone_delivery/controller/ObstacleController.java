package com.dti.drone_delivery.controller;

import com.dti.drone_delivery.model.Obstacle;
import com.dti.drone_delivery.service.ObstacleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/obstacles")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ObstacleController {
    
    private final ObstacleService obstacleService;
    
    @GetMapping
    public ResponseEntity<List<Obstacle>> getAllObstacles() {
        return ResponseEntity.ok(obstacleService.getAllObstacles());
    }
    
    @PostMapping
    public ResponseEntity<Obstacle> createObstacle(@RequestBody Obstacle obstacle) {
        return ResponseEntity.ok(obstacleService.createObstacle(obstacle));
    }
    
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> createBatchObstacles(@RequestBody List<Obstacle> obstacles) {
        List<Obstacle> created = new java.util.ArrayList<>();
        for (Obstacle obstacle : obstacles) {
            created.add(obstacleService.createObstacle(obstacle));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Obstáculos criados com sucesso");
        response.put("count", created.size());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkObstacle(
            @RequestParam Double x,
            @RequestParam Double y) {
        boolean hasObstacle = obstacleService.isPointInObstacle(x, y);
        
        Map<String, Object> response = new HashMap<>();
        response.put("x", x);
        response.put("y", y);
        response.put("hasObstacle", hasObstacle);
        response.put("message", hasObstacle ? 
            "Ponto está em zona de exclusão" : "Ponto livre para voo");
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteObstacle(@PathVariable String id) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Obstáculo removido (implementar no service)");
        return ResponseEntity.ok(response);
    }
}