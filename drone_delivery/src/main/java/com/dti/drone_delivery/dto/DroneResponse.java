package com.dti.drone_delivery.dto;

import java.time.LocalDateTime;

import com.dti.drone_delivery.model.Drone;

import lombok.Data;

@Data
public class DroneResponse {
    private String id;
    private String name;
    private Double maxWeight;
    private Double maxDistance;
    private Double currentBattery;
    private Drone.DroneStatus status;
    private Double currentX;
    private Double currentY;
    private LocalDateTime lastUpdate;
    
    public static DroneResponse fromEntity(Drone drone) {
        DroneResponse response = new DroneResponse();
        response.setId(drone.getId());
        response.setName(drone.getName());
        response.setMaxWeight(drone.getMaxWeight());
        response.setMaxDistance(drone.getMaxDistance());
        response.setCurrentBattery(drone.getCurrentBattery());
        response.setStatus(drone.getStatus());
        response.setCurrentX(drone.getCurrentX());
        response.setCurrentY(drone.getCurrentY());
        response.setLastUpdate(drone.getLastUpdate());
        return response;
    }
}