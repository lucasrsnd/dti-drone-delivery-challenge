package com.dti.drone_delivery.repository;

import com.dti.drone_delivery.model.Drone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DroneRepository extends JpaRepository<Drone, String> {
    
    List<Drone> findByStatus(Drone.DroneStatus status);
    
    @Query("SELECT d FROM Drone d WHERE d.currentBattery > :minBattery AND d.status = 'IDLE'")
    List<Drone> findAvailableDrones(Double minBattery);
    
    List<Drone> findByCurrentBatteryGreaterThan(Double batteryLevel);
    
    List<Drone> findByStatusIn(List<Drone.DroneStatus> statuses);
}