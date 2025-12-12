package com.dti.drone_delivery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dti.drone_delivery.model.Obstacle;

@Repository
public interface ObstacleRepository extends JpaRepository<Obstacle, String> {
    
    @Query("SELECT o FROM Obstacle o WHERE " +
           "(o.centerX - o.radius) <= :x AND (o.centerX + o.radius) >= :x AND " +
           "(o.centerY - o.radius) <= :y AND (o.centerY + o.radius) >= :y")
    List<Obstacle> findNearbyObstacles(Double x, Double y);
    
    List<Obstacle> findByType(String type);
}