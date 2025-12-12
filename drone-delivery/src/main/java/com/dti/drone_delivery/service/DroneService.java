package com.dti.drone_delivery.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.dti.drone_delivery.dto.DroneResponse;
import com.dti.drone_delivery.model.Drone;
import com.dti.drone_delivery.repository.DroneRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DroneService {
    
    private final DroneRepository droneRepository;
    
    public Drone createDrone(Drone drone) {
        return droneRepository.save(drone);
    }
    
    public List<DroneResponse> getAllDrones() {
        return droneRepository.findAll()
            .stream()
            .map(DroneResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    public DroneResponse getDroneById(String id) {
        return droneRepository.findById(id)
            .map(DroneResponse::fromEntity)
            .orElseThrow(() -> new RuntimeException("Drone não encontrado: " + id));
    }
    
    public List<DroneResponse> getAvailableDrones(Double minBattery) {
        return droneRepository.findAvailableDrones(minBattery)
            .stream()
            .map(DroneResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    public Drone updateDroneStatus(String id, Drone.DroneStatus status) {
        Drone drone = droneRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Drone não encontrado"));
        drone.setStatus(status);
        drone.setLastUpdate(java.time.LocalDateTime.now());
        return droneRepository.save(drone);
    }
    
    public void rechargeDrone(String id) {
        Drone drone = droneRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Drone não encontrado"));
        drone.setCurrentBattery(100.0);
        drone.setStatus(Drone.DroneStatus.IDLE);
        droneRepository.save(drone);
    }
    
    public long countByStatus(Drone.DroneStatus status) {
        return droneRepository.findByStatus(status).size();
    }
}