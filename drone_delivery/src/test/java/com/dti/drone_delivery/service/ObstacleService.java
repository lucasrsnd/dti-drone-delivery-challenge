package com.dti.drone_delivery.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dti.drone_delivery.model.Obstacle;
import com.dti.drone_delivery.repository.ObstacleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ObstacleService {
    
    private final ObstacleRepository obstacleRepository;
    
    public Obstacle createObstacle(Obstacle obstacle) {
        return obstacleRepository.save(obstacle);
    }
    
    public List<Obstacle> getAllObstacles() {
        return obstacleRepository.findAll();
    }
    
    public boolean hasObstacleBetween(double x1, double y1, double x2, double y2) {
        List<Obstacle> obstacles = obstacleRepository.findAll();
        if (obstacles.isEmpty()) {
            return false;
        }

        for (Obstacle obstacle : obstacles) {
            double distanceToLine = distancePointToLineSegment(
                obstacle.getCenterX(), obstacle.getCenterY(),
                x1, y1, x2, y2
            );
            
            // Verifica se a distância é menor ou igual ao raio do obstáculo
            if (distanceToLine <= obstacle.getRadius()) {
                return true;
            }
        }
        return false;
    }
    
    // CORREÇÃO: Método para calcular distância de ponto a SEGMENTO de reta
    private double distancePointToLineSegment(double px, double py, 
                                             double x1, double y1, double x2, double y2) {
        // Vetores
        double A = px - x1;
        double B = py - y1;
        double C = x2 - x1;
        double D = y2 - y1;

        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        
        // Se o segmento tem comprimento zero
        if (lenSq == 0) {
            return Math.sqrt(A * A + B * B);
        }

        // Parâmetro de projeção
        double param = dot / lenSq;

        double xx, yy;

        if (param < 0) {
            // Ponto mais próximo é x1, y1
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            // Ponto mais próximo é x2, y2
            xx = x2;
            yy = y2;
        } else {
            // Ponto mais próximo está no segmento
            xx = x1 + param * C;
            yy = y1 + param * D;
        }

        double dx = px - xx;
        double dy = py - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    public boolean isPointInObstacle(double x, double y) {
        return obstacleRepository.findNearbyObstacles(x, y)
            .stream()
            .anyMatch(obstacle -> obstacle.isPointInside(x, y));
    }
}