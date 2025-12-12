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

        for (Obstacle obstacle : obstacles) {
            double distanceToLine = distancePointToLine(
                obstacle.getCenterX(), obstacle.getCenterY(),
                x1, y1, x2, y2
            );
            
            if (distanceToLine <= obstacle.getRadius() + 0.5) {
                return true;
            }
        }
        return false;
    }
    
    private double distancePointToLine(double px, double py, 
                                       double x1, double y1, double x2, double y2) {
        double numerator = Math.abs(
            (y2 - y1) * px - (x2 - x1) * py + x2 * y1 - y2 * x1
        );
        double denominator = Math.sqrt(
            Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2)
        );
        return numerator / denominator;
    }
    
    public boolean isPointInObstacle(double x, double y) {
        return obstacleRepository.findNearbyObstacles(x, y)
            .stream()
            .anyMatch(obstacle -> obstacle.isPointInside(x, y));
    }
}