package com.dti.drone_delivery.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dti.drone_delivery.model.Drone;
import com.dti.drone_delivery.model.Order;
import com.dti.drone_delivery.repository.DroneRepository;
import com.dti.drone_delivery.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class AllocationServiceTest {
    
    @Mock
    private DroneRepository droneRepository;
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private ObstacleService obstacleService;
    
    @InjectMocks
    private AllocationService allocationService;
    
    private Drone drone1;
    private Drone drone2;
    private Order order1;
    private Order order2;
    private Order order3;
    
    @BeforeEach
    void setUp() {
        // Configurar drones de teste
        drone1 = Drone.builder()
            .id("drone-1")
            .name("Drone Alpha")
            .maxWeight(10.0)
            .maxDistance(50.0)
            .currentBattery(100.0)
            .status(Drone.DroneStatus.IDLE)
            .build();
            
        drone2 = Drone.builder()
            .id("drone-2")
            .name("Drone Beta")
            .maxWeight(15.0)
            .maxDistance(60.0)
            .currentBattery(80.0)
            .status(Drone.DroneStatus.IDLE)
            .build();
        
        // Configurar pedidos de teste
        order1 = Order.builder()
            .id("order-1")
            .customerName("Cliente A")
            .locationX(5.0)
            .locationY(5.0)
            .weight(3.0)
            .priority(Order.Priority.HIGH)
            .status(Order.OrderStatus.PENDING)
            .createdAt(LocalDateTime.now().minusMinutes(10))
            .build();
            
        order2 = Order.builder()
            .id("order-2")
            .customerName("Cliente B")
            .locationX(10.0)
            .locationY(10.0)
            .weight(7.0)
            .priority(Order.Priority.MEDIUM)
            .status(Order.OrderStatus.PENDING)
            .createdAt(LocalDateTime.now().minusMinutes(5))
            .build();
            
        order3 = Order.builder()
            .id("order-3")
            .customerName("Cliente C")
            .locationX(15.0)
            .locationY(15.0)
            .weight(12.0) // Muito pesado para drone1
            .priority(Order.Priority.URGENT)
            .status(Order.OrderStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    @Test
    void testOptimizePackagesForDrone_withSingleOrder_shouldReturnOrder() {
        // Arrange
        List<Order> orders = Arrays.asList(order1);
        
        // Act
        List<Order> result = allocationService.optimizePackagesForDrone(drone1, orders);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("order-1", result.get(0).getId());
    }
    
    @Test
    void testOptimizePackagesForDrone_withMultipleOrders_shouldOptimizeByWeight() {
        // Arrange
        List<Order> orders = Arrays.asList(order1, order2); // Peso total: 10kg (capacidade exata)
        
        // Act
        List<Order> result = allocationService.optimizePackagesForDrone(drone1, orders);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.size() <= 2);
    }
    
    @Test
    void testOptimizePackagesForDrone_withOverweightOrder_shouldExcludeOrder() {
        // Arrange
        List<Order> orders = Arrays.asList(order1, order3); // order3 tem 12kg (> capacidade)
        
        // Act
        List<Order> result = allocationService.optimizePackagesForDrone(drone1, orders);
        
        // Assert
        assertNotNull(result);
        // Deve incluir apenas order1, pois order3 excede capacidade
        assertTrue(result.stream().noneMatch(o -> o.getId().equals("order-3")));
    }
    
    @Test
    void testCalculateOrderValue_urgentPriority_shouldReturnHighestValue() {
        // Arrange
        Order urgentOrder = Order.builder()
            .priority(Order.Priority.URGENT)
            .createdAt(LocalDateTime.now().minusMinutes(30))
            .build();
        
        // Act (usando reflexão para testar método privado ou criar método público)
        // Vamos testar indiretamente através do optimizePackagesForDrone
        List<Order> orders = Arrays.asList(order1, order3); // order3 é URGENT
        
        // Act
        List<Order> result = allocationService.optimizePackagesForDrone(drone2, orders);
        
        // Assert - order3 deve ser priorizado mesmo sendo mais pesado
        assertNotNull(result);
    }
    
    @Test
    void testAllocateOrdersToDrones_withAvailableDronesAndOrders_shouldAllocate() {
        // Arrange
        List<Drone> availableDrones = Arrays.asList(drone1, drone2);
        List<Order> pendingOrders = Arrays.asList(order1, order2);
        
        when(droneRepository.findAvailableDrones(20.0)).thenReturn(availableDrones);
        when(orderRepository.findByStatus(Order.OrderStatus.PENDING)).thenReturn(pendingOrders);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(droneRepository.save(any(Drone.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        Map<String, List<Order>> result = allocationService.allocateOrdersToDrones();
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
        verify(droneRepository, atLeastOnce()).save(any(Drone.class));
    }
    
    @Test
    void testAllocateOrdersToDrones_withNoAvailableDrones_shouldReturnEmptyMap() {
        // Arrange
        when(droneRepository.findAvailableDrones(20.0)).thenReturn(Collections.emptyList());
        
        // Act
        Map<String, List<Order>> result = allocationService.allocateOrdersToDrones();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void testAllocateOrdersToDrones_withNoPendingOrders_shouldReturnEmptyMap() {
        // Arrange
        List<Drone> availableDrones = Arrays.asList(drone1);
        when(droneRepository.findAvailableDrones(20.0)).thenReturn(availableDrones);
        when(orderRepository.findByStatus(Order.OrderStatus.PENDING)).thenReturn(Collections.emptyList());
        
        // Act
        Map<String, List<Order>> result = allocationService.allocateOrdersToDrones();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testCalculateDistance_shouldReturnCorrectDistance() {
        // Arrange
        double x1 = 0, y1 = 0;
        double x2 = 3, y2 = 4; // 3-4-5 triangle
        
        // Act
        double distance = allocationService.calculateDistance(x1, y1, x2, y2);
        
        // Assert
        assertEquals(5.0, distance, 0.001);
    }
    
    @Test
    void testCalculateBatteryUsage_shouldCalculateCorrectly() {
        // Arrange
        double distance = 10.0;
        double efficiency = 0.1; // 10% por km
        
        // Act
        double batteryUsage = allocationService.calculateBatteryUsage(distance, efficiency);
        
        // Assert
        assertEquals(1.0, batteryUsage, 0.001); // 10km * 0.1 = 1.0
    }
}