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
        // Arrange - NÃO mocke o save, apenas verifique que não lança exceção
        
        // Act & Assert - Apenas verifique que o método executa sem exceções
        assertDoesNotThrow(() -> {
            simulationService.generateRandomOrder();
        });
        
        // Não verifique save() pois é random e pode não chamar
    }
    
    @Test
    void testUpdateDroneStates_idleDrone_shouldRemainIdle() {
        // Arrange
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
        
        // Act
        simulationService.updateDroneStates();
        
        // Assert - Drone IDLE deve permanecer IDLE
        assertEquals(Drone.DroneStatus.IDLE, idleDrone.getStatus());
        verify(droneRepository).save(idleDrone);
    }
    
    @Test
    void testUpdateDroneStates_loadingDrone_shouldChangeToFlying() {
        // Arrange
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
        
        // Act
        simulationService.updateDroneStates();
        
        // Assert - LOADING deve mudar para FLYING
        assertEquals(Drone.DroneStatus.FLYING, loadingDrone.getStatus());
        verify(droneRepository).save(loadingDrone);
    }
    
    @Test
    void testUpdateDroneStates_flyingDroneWithLowBattery_shouldReturnToBase() {
        // Arrange
        Drone flyingDrone = Drone.builder()
            .id("drone-1")
            .name("Test Drone")
            .status(Drone.DroneStatus.FLYING)
            .currentBattery(15.0) // Bateria baixa
            .currentX(20.0)
            .currentY(20.0)
            .baseX(0.0)
            .baseY(0.0)
            .build();
            
        when(droneRepository.findAll()).thenReturn(Arrays.asList(flyingDrone));
        // NÃO mocke findByAssignedDroneId - deixe retornar null/empty naturalmente
        when(droneRepository.save(any(Drone.class))).thenReturn(flyingDrone);
        
        // Act
        simulationService.updateDroneStates();
        
        // Assert - Com bateria < 20, deve mudar para RETURNING
        assertEquals(Drone.DroneStatus.RETURNING, flyingDrone.getStatus());
        assertTrue(flyingDrone.getCurrentBattery() < 20.0); // Consumiu bateria
        verify(droneRepository).save(flyingDrone);
    }
    
    @Test
    void testUpdateDroneStates_flyingDroneWithAssignedOrder_shouldMoveAndConsumeBattery() {
        // Arrange
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
        
        // Act
        simulationService.updateDroneStates();
        
        // Assert - Deve consumir bateria
        assertTrue(flyingDrone.getCurrentBattery() < 50.0);
        verify(droneRepository).save(flyingDrone);
    }
    
    @Test
    void testUpdateDroneStates_deliveringDrone_shouldDeliverOrders() {
        // Arrange
        Drone deliveringDrone = Drone.builder()
            .id("drone-1")
            .name("Test Drone")
            .status(Drone.DroneStatus.DELIVERING)
            .currentBattery(40.0)
            .currentX(30.0) // Já no destino
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
        
        // Act
        simulationService.updateDroneStates();
        
        // Assert - Pedido deve ser marcado como DELIVERED
        assertEquals(Order.OrderStatus.DELIVERED, pendingOrder.getStatus());
        assertNotNull(pendingOrder.getDeliveredAt());
        assertEquals(Drone.DroneStatus.RETURNING, deliveringDrone.getStatus());
        verify(orderRepository).save(pendingOrder);
        verify(droneRepository).save(deliveringDrone);
    }
    
    @Test
    void testUpdateDroneStates_returningDroneAtBase_shouldChargeOrIdle() {
        // Arrange
        Drone returningDrone = Drone.builder()
            .id("drone-1")
            .name("Test Drone")
            .status(Drone.DroneStatus.RETURNING)
            .currentBattery(25.0) // < 30, deve carregar
            .currentX(0.1) // Quase na base
            .currentY(0.1)
            .baseX(0.0)
            .baseY(0.0)
            .build();
            
        when(droneRepository.findAll()).thenReturn(Arrays.asList(returningDrone));
        when(droneRepository.save(any(Drone.class))).thenReturn(returningDrone);
        
        // Act
        simulationService.updateDroneStates();
        
        // Assert - Com bateria < 30, deve mudar para CHARGING
        assertEquals(Drone.DroneStatus.CHARGING, returningDrone.getStatus());
        verify(droneRepository).save(returningDrone);
    }
    
    @Test
    void testUpdateDroneStates_chargingDrone_shouldRechargeBattery() {
        // Arrange
        Drone chargingDrone = Drone.builder()
            .id("drone-1")
            .name("Test Drone")
            .status(Drone.DroneStatus.CHARGING)
            .currentBattery(50.0)
            .build();
            
        when(droneRepository.findAll()).thenReturn(Arrays.asList(chargingDrone));
        when(droneRepository.save(any(Drone.class))).thenReturn(chargingDrone);
        
        // Act
        simulationService.updateDroneStates();
        
        // Assert - Bateria deve aumentar
        assertTrue(chargingDrone.getCurrentBattery() > 50.0);
        verify(droneRepository).save(chargingDrone);
    }
    
    @Test
    void testUpdateDroneStates_chargingDroneFullBattery_shouldBecomeIdle() {
        // Arrange
        Drone chargingDrone = Drone.builder()
            .id("drone-1")
            .name("Test Drone")
            .status(Drone.DroneStatus.CHARGING)
            .currentBattery(85.0) // Após recarga ficará >= 80
            .build();
            
        when(droneRepository.findAll()).thenReturn(Arrays.asList(chargingDrone));
        when(droneRepository.save(any(Drone.class))).thenReturn(chargingDrone);
        
        // Act
        simulationService.updateDroneStates();
        
        // Assert - Com bateria >= 80, deve voltar para IDLE
        assertEquals(Drone.DroneStatus.IDLE, chargingDrone.getStatus());
        verify(droneRepository).save(chargingDrone);
    }
    
    @Test
    void testResetSimulation_shouldClearOrdersAndResetDrones() {
        // Arrange
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
        
        // Act
        simulationService.resetSimulation();
        
        // Assert
        verify(orderRepository).deleteAll();
        verify(droneRepository).save(testDrone);
        assertEquals(Drone.DroneStatus.IDLE, testDrone.getStatus());
        assertEquals(100.0, testDrone.getCurrentBattery());
        assertEquals(0.0, testDrone.getCurrentX());
        assertEquals(0.0, testDrone.getCurrentY());
    }
    
    @Test
    void testMoveDroneToDestination_withNoAssignedOrders_shouldConsumeBattery() {
        // Este teste verifica o comportamento quando não há pedidos atribuídos
        
        // Arrange
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
        
        // Act
        simulationService.updateDroneStates();
        
        // Assert - Deve consumir bateria mesmo sem destino específico
        assertTrue(flyingDrone.getCurrentBattery() < 60.0);
        verify(droneRepository).save(flyingDrone);
    }
}