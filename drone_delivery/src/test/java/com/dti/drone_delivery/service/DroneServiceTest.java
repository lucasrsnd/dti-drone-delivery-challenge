package com.dti.drone_delivery.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

        when(droneRepository.save(any(Drone.class))).thenReturn(drone);
        
        Drone result = droneService.createDrone(drone);

        assertNotNull(result);
        assertEquals("Test Drone", result.getName());
        verify(droneRepository).save(drone);
    }
    
    @Test
    void testGetAllDrones_shouldReturnList() {
        Drone drone2 = Drone.builder()
            .id("drone-test-2")
            .name("Test Drone 2")
            .build();
            
        when(droneRepository.findAll()).thenReturn(Arrays.asList(drone, drone2));

        List<DroneResponse> result = droneService.getAllDrones();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(droneRepository).findAll();
    }
    
    @Test
    void testGetDroneById_shouldReturnDrone() {

        when(droneRepository.findById("drone-test-1")).thenReturn(Optional.of(drone));

        DroneResponse result = droneService.getDroneById("drone-test-1");

        assertNotNull(result);
        assertEquals("Test Drone", result.getName());
        verify(droneRepository).findById("drone-test-1");
    }
    
    @Test
    void testGetDroneById_notFound_shouldThrowException() {
        when(droneRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            droneService.getDroneById("non-existent");
        });
        verify(droneRepository).findById("non-existent");
    }
    
    @Test
    void testGetAvailableDrones_shouldReturnAvailableDrones() {
        when(droneRepository.findAvailableDrones(20.0)).thenReturn(Arrays.asList(drone));
        
        List<DroneResponse> result = droneService.getAvailableDrones(20.0);
  
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(droneRepository).findAvailableDrones(20.0);
    }
    
    @Test
    void testUpdateDroneStatus_shouldUpdateStatus() {

        when(droneRepository.findById("drone-test-1")).thenReturn(Optional.of(drone));
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        Drone result = droneService.updateDroneStatus("drone-test-1", Drone.DroneStatus.FLYING);
        
        assertNotNull(result);
        assertEquals(Drone.DroneStatus.FLYING, result.getStatus());
        assertNotNull(result.getLastUpdate());
        verify(droneRepository).save(drone);
    }
    
    @Test
    void testRechargeDrone_shouldSetBatteryTo100() {
        when(droneRepository.findById("drone-test-1")).thenReturn(Optional.of(drone));
        when(droneRepository.save(any(Drone.class))).thenReturn(drone);

        droneService.rechargeDrone("drone-test-1");
 
        assertEquals(100.0, drone.getCurrentBattery());
        assertEquals(Drone.DroneStatus.IDLE, drone.getStatus());
        verify(droneRepository).save(drone);
    }
    
    @Test
    void testCountByStatus_shouldReturnCount() {
        when(droneRepository.findByStatus(Drone.DroneStatus.IDLE))
            .thenReturn(Arrays.asList(drone, drone, drone));
 
        long count = droneService.countByStatus(Drone.DroneStatus.IDLE);

        assertEquals(3, count);
        verify(droneRepository).findByStatus(Drone.DroneStatus.IDLE);
    }
}