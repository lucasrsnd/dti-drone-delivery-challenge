package com.dti.drone_delivery.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dti.drone_delivery.dto.DroneResponse;
import com.dti.drone_delivery.model.Drone;
import com.dti.drone_delivery.repository.DroneRepository;

@ExtendWith(MockitoExtension.class)
class DroneServiceTest {
    
    @Mock
    private DroneRepository droneRepository;
    
    @InjectMocks
    private DroneService droneService;
    
    private Drone drone;
    
    @BeforeEach
    void setUp() {
        drone = Drone.builder()
            .id("drone-test-1")
            .name("Test Drone")
            .maxWeight(10.0)
            .maxDistance(50.0)
            .batteryCapacity(100.0)
            .currentBattery(85.0)
            .status(Drone.DroneStatus.IDLE)
            .currentX(0.0)
            .currentY(0.0)
            .lastUpdate(LocalDateTime.now())
            .build();
    }
    
    @Test
    void testCreateDrone_shouldSaveAndReturnDrone() {
        // Arrange
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);
        
        // Act
        Drone result = droneService.createDrone(drone);
        
        // Assert
        assertNotNull(result);
        assertEquals("Test Drone", result.getName());
        verify(droneRepository).save(drone);
    }
    
    @Test
    void testGetAllDrones_shouldReturnList() {
        // Arrange
        Drone drone2 = Drone.builder()
            .id("drone-test-2")
            .name("Test Drone 2")
            .build();
            
        when(droneRepository.findAll()).thenReturn(Arrays.asList(drone, drone2));
        
        // Act
        List<DroneResponse> result = droneService.getAllDrones();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(droneRepository).findAll();
    }
    
    @Test
    void testGetDroneById_shouldReturnDrone() {
        // Arrange
        when(droneRepository.findById("drone-test-1")).thenReturn(Optional.of(drone));
        
        // Act
        DroneResponse result = droneService.getDroneById("drone-test-1");
        
        // Assert
        assertNotNull(result);
        assertEquals("Test Drone", result.getName());
        verify(droneRepository).findById("drone-test-1");
    }
    
    @Test
    void testGetDroneById_notFound_shouldThrowException() {
        // Arrange
        when(droneRepository.findById("non-existent")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            droneService.getDroneById("non-existent");
        });
        verify(droneRepository).findById("non-existent");
    }
    
    @Test
    void testGetAvailableDrones_shouldReturnAvailableDrones() {
        // Arrange
        when(droneRepository.findAvailableDrones(20.0)).thenReturn(Arrays.asList(drone));
        
        // Act
        List<DroneResponse> result = droneService.getAvailableDrones(20.0);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(droneRepository).findAvailableDrones(20.0);
    }
    
    @Test
    void testUpdateDroneStatus_shouldUpdateStatus() {
        // Arrange
        when(droneRepository.findById("drone-test-1")).thenReturn(Optional.of(drone));
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);
        
        // Act
        Drone result = droneService.updateDroneStatus("drone-test-1", Drone.DroneStatus.FLYING);
        
        // Assert
        assertNotNull(result);
        assertEquals(Drone.DroneStatus.FLYING, result.getStatus());
        assertNotNull(result.getLastUpdate());
        verify(droneRepository).save(drone);
    }
    
    @Test
    void testRechargeDrone_shouldSetBatteryTo100() {
        // Arrange
        when(droneRepository.findById("drone-test-1")).thenReturn(Optional.of(drone));
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);
        
        // Act
        droneService.rechargeDrone("drone-test-1");
        
        // Assert
        assertEquals(100.0, drone.getCurrentBattery());
        assertEquals(Drone.DroneStatus.IDLE, drone.getStatus());
        verify(droneRepository).save(drone);
    }
    
    @Test
    void testCountByStatus_shouldReturnCount() {
        // Arrange
        when(droneRepository.findByStatus(Drone.DroneStatus.IDLE))
            .thenReturn(Arrays.asList(drone, drone, drone)); // 3 drones ociosos
        
        // Act
        long count = droneService.countByStatus(Drone.DroneStatus.IDLE);
        
        // Assert
        assertEquals(3, count);
        verify(droneRepository).findByStatus(Drone.DroneStatus.IDLE);
    }
}