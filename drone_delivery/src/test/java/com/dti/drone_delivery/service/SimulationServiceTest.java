package com.dti.drone_delivery.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dti.drone_delivery.model.Drone;
import com.dti.drone_delivery.model.Order;
import com.dti.drone_delivery.repository.DroneRepository;
import com.dti.drone_delivery.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class SimulationServiceTest {
    
    @Mock
    private DroneRepository droneRepository;
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private AllocationService allocationService;
    
    @Mock
    private ScheduledExecutorService scheduler;
    
    @InjectMocks
    private SimulationService simulationService;
    
    private Drone drone;
    private Order order;
    
    @BeforeEach
    void setUp() {
        drone = Drone.builder()
            .id("drone-1")
            .name("Test Drone")
            .maxWeight(10.0)
            .maxDistance(50.0)
            .batteryCapacity(100.0)
            .currentBattery(85.0)
            .status(Drone.DroneStatus.IDLE)
            .baseX(0.0)
            .baseY(0.0)
            .currentX(0.0)
            .currentY(0.0)
            .lastUpdate(LocalDateTime.now())
            .build();
            
        order = Order.builder()
            .id("order-1")
            .customerName("Test Customer")
            .locationX(10.0)
            .locationY(10.0)
            .weight(5.0)
            .priority(Order.Priority.MEDIUM)
            .status(Order.OrderStatus.PENDING)
            .assignedDroneId("drone-1")
            .build();
    }
    
    @Test
    void testGenerateRandomOrder_withChance_shouldNotThrowException() {

        assertDoesNotThrow(() -> {
            simulationService.generateRandomOrder();
        });

    }
    
    @Test
    void testUpdateDroneStates_idleDrone_shouldRemainIdle() {
        Drone idleDrone = Drone.builder()
            .id("drone-1")
            .name("Test Drone")
            .status(Drone.DroneStatus.IDLE)
            .currentBattery(100.0)
            .currentX(0.0)
            .currentY(0.0)
            .baseX(0.0)
            .baseY(0.0)
            .build();
            
        when(droneRepository.findAll()).thenReturn(Arrays.asList(idleDrone));
        when(droneRepository.save(any(Drone.class))).thenReturn(idleDrone);

        simulationService.updateDroneStates();

        assertEquals(Drone.DroneStatus.IDLE, idleDrone.getStatus());
        verify(droneRepository).save(idleDrone);
    }
    
    @Test
    void testUpdateDroneStates_loadingDrone_shouldChangeToFlying() {
        Drone loadingDrone = Drone.builder()
            .id("drone-1")
            .name("Test Drone")
            .status(Drone.DroneStatus.LOADING)
            .currentBattery(100.0)
            .currentX(0.0)
            .currentY(0.0)
            .build();
            
        when(droneRepository.findAll()).thenReturn(Arrays.asList(loadingDrone));
        when(droneRepository.save(any(Drone.class))).thenReturn(loadingDrone);
 
        simulationService.updateDroneStates();
    
        assertEquals(Drone.DroneStatus.FLYING, loadingDrone.getStatus());
        verify(droneRepository).save(loadingDrone);
    }
    
    @Test
    void testUpdateDroneStates_flyingDroneWithLowBattery_shouldReturnToBase() {
        Drone flyingDrone = Drone.builder()
            .id("drone-1")
            .name("Test Drone")
            .status(Drone.DroneStatus.FLYING)
            .currentBattery(15.0)
            .currentX(20.0)
            .currentY(20.0)
            .baseX(0.0)
            .baseY(0.0)
            .build();
            
        when(droneRepository.findAll()).thenReturn(Arrays.asList(flyingDrone));
        when(droneRepository.save(any(Drone.class))).thenReturn(flyingDrone);

        simulationService.updateDroneStates();

        assertEquals(Drone.DroneStatus.RETURNING, flyingDrone.getStatus());
        assertTrue(flyingDrone.getCurrentBattery() < 20.0);
        verify(droneRepository).save(flyingDrone);
    }
    
    @Test
    void testUpdateDroneStates_flyingDroneWithAssignedOrder_shouldMoveAndConsumeBattery() {

        Drone flyingDrone = Drone.builder()
            .id("drone-1")
            .name("Test Drone")
            .status(Drone.DroneStatus.FLYING)
            .currentBattery(50.0)
            .currentX(0.0)
            .currentY(0.0)
            .baseX(0.0)
            .baseY(0.0)
            .build();
            
        Order assignedOrder = Order.builder()
            .id("order-1")
            .locationX(30.0)
            .locationY(40.0)
            .assignedDroneId("drone-1")
            .status(Order.OrderStatus.ASSIGNED)
            .build();
            
        when(droneRepository.findAll()).thenReturn(Arrays.asList(flyingDrone));
        when(orderRepository.findByAssignedDroneId("drone-1")).thenReturn(Arrays.asList(assignedOrder));
        when(droneRepository.save(any(Drone.class))).thenReturn(flyingDrone);

        simulationService.updateDroneStates();
        
        assertTrue(flyingDrone.getCurrentBattery() < 50.0);
        verify(droneRepository).save(flyingDrone);
    }
    
    @Test
    void testUpdateDroneStates_deliveringDrone_shouldDeliverOrders() {
        Drone deliveringDrone = Drone.builder()
            .id("drone-1")
            .name("Test Drone")
            .status(Drone.DroneStatus.DELIVERING)
            .currentBattery(40.0)
            .currentX(30.0)
            .currentY(40.0)
            .baseX(0.0)
            .baseY(0.0)
            .build();
            
        Order pendingOrder = Order.builder()
            .id("order-1")
            .customerName("Cliente Teste")
            .status(Order.OrderStatus.ASSIGNED)
            .assignedDroneId("drone-1")
            .build();
            
        when(droneRepository.findAll()).thenReturn(Arrays.asList(deliveringDrone));
        when(orderRepository.findByAssignedDroneId("drone-1")).thenReturn(Arrays.asList(pendingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(pendingOrder);
        when(droneRepository.save(any(Drone.class))).thenReturn(deliveringDrone);

        simulationService.updateDroneStates();

        assertEquals(Order.OrderStatus.DELIVERED, pendingOrder.getStatus());
        assertNotNull(pendingOrder.getDeliveredAt());
        assertEquals(Drone.DroneStatus.RETURNING, deliveringDrone.getStatus());
        verify(orderRepository).save(pendingOrder);
        verify(droneRepository).save(deliveringDrone);
    }
    
    @Test
    void testUpdateDroneStates_returningDroneAtBase_shouldChargeOrIdle() {
        Drone returningDrone = Drone.builder()
            .id("drone-1")
            .name("Test Drone")
            .status(Drone.DroneStatus.RETURNING)
            .currentBattery(25.0)
            .currentX(0.1)
            .currentY(0.1)
            .baseX(0.0)
            .baseY(0.0)
            .build();
            
        when(droneRepository.findAll()).thenReturn(Arrays.asList(returningDrone));
        when(droneRepository.save(any(Drone.class))).thenReturn(returningDrone);
        
        simulationService.updateDroneStates();

        assertEquals(Drone.DroneStatus.CHARGING, returningDrone.getStatus());
        verify(droneRepository).save(returningDrone);
    }
    
    @Test
    void testUpdateDroneStates_chargingDrone_shouldRechargeBattery() {

        Drone chargingDrone = Drone.builder()
            .id("drone-1")
            .name("Test Drone")
            .status(Drone.DroneStatus.CHARGING)
            .currentBattery(50.0)
            .build();
            
        when(droneRepository.findAll()).thenReturn(Arrays.asList(chargingDrone));
        when(droneRepository.save(any(Drone.class))).thenReturn(chargingDrone);

        simulationService.updateDroneStates();

        assertTrue(chargingDrone.getCurrentBattery() > 50.0);
        verify(droneRepository).save(chargingDrone);
    }
    
    @Test
    void testUpdateDroneStates_chargingDroneFullBattery_shouldBecomeIdle() {
        Drone chargingDrone = Drone.builder()
            .id("drone-1")
            .name("Test Drone")
            .status(Drone.DroneStatus.CHARGING)
            .currentBattery(85.0)
            .build();
            
        when(droneRepository.findAll()).thenReturn(Arrays.asList(chargingDrone));
        when(droneRepository.save(any(Drone.class))).thenReturn(chargingDrone);

        simulationService.updateDroneStates();

        assertEquals(Drone.DroneStatus.IDLE, chargingDrone.getStatus());
        verify(droneRepository).save(chargingDrone);
    }
    
    @Test
    void testResetSimulation_shouldClearOrdersAndResetDrones() {

        Drone testDrone = Drone.builder()
            .id("drone-1")
            .name("Test Drone")
            .status(Drone.DroneStatus.FLYING)
            .currentBattery(50.0)
            .currentX(10.0)
            .currentY(10.0)
            .baseX(0.0)
            .baseY(0.0)
            .build();
            
        when(droneRepository.findAll()).thenReturn(Arrays.asList(testDrone));
        when(droneRepository.save(any(Drone.class))).thenReturn(testDrone);

        simulationService.resetSimulation();
 
        verify(orderRepository).deleteAll();
        verify(droneRepository).save(testDrone);
        assertEquals(Drone.DroneStatus.IDLE, testDrone.getStatus());
        assertEquals(100.0, testDrone.getCurrentBattery());
        assertEquals(0.0, testDrone.getCurrentX());
        assertEquals(0.0, testDrone.getCurrentY());
    }
    
    @Test
    void testMoveDroneToDestination_withNoAssignedOrders_shouldConsumeBattery() {

        Drone flyingDrone = Drone.builder()
            .id("drone-1")
            .status(Drone.DroneStatus.FLYING)
            .currentBattery(60.0)
            .currentX(0.0)
            .currentY(0.0)
            .baseX(0.0)
            .baseY(0.0)
            .build();
            
        when(droneRepository.findAll()).thenReturn(Arrays.asList(flyingDrone));
        when(orderRepository.findByAssignedDroneId("drone-1")).thenReturn(Arrays.asList());
        when(droneRepository.save(any(Drone.class))).thenReturn(flyingDrone);

        simulationService.updateDroneStates();

        assertTrue(flyingDrone.getCurrentBattery() < 60.0);
        verify(droneRepository).save(flyingDrone);
    }
}