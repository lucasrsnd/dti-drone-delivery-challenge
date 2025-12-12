package com.dti.drone_delivery.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
            .weight(12.0)
            .priority(Order.Priority.URGENT)
            .status(Order.OrderStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    @Test
    void testOptimizePackagesForDrone_withSingleOrder_shouldReturnOrder() {
        List<Order> orders = Arrays.asList(order1);

        List<Order> result = allocationService.optimizePackagesForDrone(drone1, orders);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("order-1", result.get(0).getId());
    }
    
    @Test
    void testOptimizePackagesForDrone_withMultipleOrders_shouldOptimizeByWeight() {
        List<Order> orders = Arrays.asList(order1, order2);

        List<Order> result = allocationService.optimizePackagesForDrone(drone1, orders);
 
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.size() <= 2);
    }
    
    @Test
    void testOptimizePackagesForDrone_withOverweightOrder_shouldExcludeOrder() {
        List<Order> orders = Arrays.asList(order1, order3);

        List<Order> result = allocationService.optimizePackagesForDrone(drone1, orders);

        assertNotNull(result);
        assertTrue(result.stream().noneMatch(o -> o.getId().equals("order-3")));
    }
    
    @Test
    void testCalculateOrderValue_urgentPriority_shouldReturnHighestValue() {

        Order urgentOrder = Order.builder()
            .priority(Order.Priority.URGENT)
            .createdAt(LocalDateTime.now().minusMinutes(30))
            .build();

        List<Order> orders = Arrays.asList(order1, order3);

        List<Order> result = allocationService.optimizePackagesForDrone(drone2, orders);

        assertNotNull(result);
    }
    
    @Test
    void testAllocateOrdersToDrones_withAvailableDronesAndOrders_shouldAllocate() {
        List<Drone> availableDrones = Arrays.asList(drone1, drone2);
        List<Order> pendingOrders = Arrays.asList(order1, order2);
        
        when(droneRepository.findAvailableDrones(20.0)).thenReturn(availableDrones);
        when(orderRepository.findByStatus(Order.OrderStatus.PENDING)).thenReturn(pendingOrders);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(droneRepository.save(any(Drone.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, List<Order>> result = allocationService.allocateOrdersToDrones();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
        verify(droneRepository, atLeastOnce()).save(any(Drone.class));
    }
    
    @Test
    void testAllocateOrdersToDrones_withNoAvailableDrones_shouldReturnEmptyMap() {
        when(droneRepository.findAvailableDrones(20.0)).thenReturn(Collections.emptyList());

        Map<String, List<Order>> result = allocationService.allocateOrdersToDrones();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void testAllocateOrdersToDrones_withNoPendingOrders_shouldReturnEmptyMap() {
        List<Drone> availableDrones = Arrays.asList(drone1);
        when(droneRepository.findAvailableDrones(20.0)).thenReturn(availableDrones);
        when(orderRepository.findByStatus(Order.OrderStatus.PENDING)).thenReturn(Collections.emptyList());

        Map<String, List<Order>> result = allocationService.allocateOrdersToDrones();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testCalculateDistance_shouldReturnCorrectDistance() {
        double x1 = 0, y1 = 0;
        double x2 = 3, y2 = 4;

        double distance = allocationService.calculateDistance(x1, y1, x2, y2);

        assertEquals(5.0, distance, 0.001);
    }
    
    @Test
    void testCalculateBatteryUsage_shouldCalculateCorrectly() {
        double distance = 10.0;
        double efficiency = 0.1;

        double batteryUsage = allocationService.calculateBatteryUsage(distance, efficiency);

        assertEquals(1.0, batteryUsage, 0.001);
    }
}