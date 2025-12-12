package com.dti.drone_delivery.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.dti.drone_delivery.model.Delivery;
import com.dti.drone_delivery.repository.DeliveryRepository;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {
    
    @Mock
    private DeliveryRepository deliveryRepository;
    
    @InjectMocks
    private DeliveryService deliveryService;
    
    private Delivery delivery;
    
    @BeforeEach
    void setUp() {
        delivery = Delivery.builder()
            .id("delivery-1")
            .orderId("order-1")
            .droneId("drone-1")
            .startedAt(LocalDateTime.now().minusMinutes(30))
            .status(Delivery.DeliveryStatus.IN_PROGRESS)
            .build();
    }
    
    @Test
    void testGetAllDeliveries_shouldReturnAll() {
        Delivery delivery2 = Delivery.builder().id("delivery-2").build();
        when(deliveryRepository.findAll()).thenReturn(Arrays.asList(delivery, delivery2));

        List<Delivery> result = deliveryService.getAllDeliveries();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(deliveryRepository).findAll();
    }
    
    @Test
    void testGetDeliveriesByDrone_shouldFilterByDrone() {
        when(deliveryRepository.findByDroneId("drone-1"))
            .thenReturn(Arrays.asList(delivery));

        List<Delivery> result = deliveryService.getDeliveriesByDrone("drone-1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("drone-1", result.get(0).getDroneId());
        verify(deliveryRepository).findByDroneId("drone-1");
    }
    
    @Test
    void testGetDeliveriesByStatus_shouldFilterByStatus() {
        when(deliveryRepository.findByStatus(Delivery.DeliveryStatus.IN_PROGRESS))
            .thenReturn(Arrays.asList(delivery));

        List<Delivery> result = deliveryService.getDeliveriesByStatus(Delivery.DeliveryStatus.IN_PROGRESS);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Delivery.DeliveryStatus.IN_PROGRESS, result.get(0).getStatus());
        verify(deliveryRepository).findByStatus(Delivery.DeliveryStatus.IN_PROGRESS);
    }
    
    @Test
    void testCreateDelivery_shouldSaveNewDelivery() {
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);

        Delivery result = deliveryService.createDelivery("order-1", "drone-1");

        assertNotNull(result);
        assertEquals("order-1", result.getOrderId());
        assertEquals("drone-1", result.getDroneId());
        assertEquals(Delivery.DeliveryStatus.IN_PROGRESS, result.getStatus());
        assertNotNull(result.getStartedAt());
        verify(deliveryRepository).save(any(Delivery.class));
    }
    
    @Test
    void testCompleteDelivery_shouldUpdateStatusAndMetrics() {
        Delivery inProgressDelivery = Delivery.builder()
            .id("delivery-1")
            .startedAt(LocalDateTime.now().minusMinutes(15))
            .status(Delivery.DeliveryStatus.IN_PROGRESS)
            .build();
            
        when(deliveryRepository.findById("delivery-1")).thenReturn(Optional.of(inProgressDelivery));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(inProgressDelivery);

        Delivery result = deliveryService.completeDelivery("delivery-1", 10.5, 15.0);
        
        assertNotNull(result);
        assertEquals(Delivery.DeliveryStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getDeliveredAt());
        assertEquals(10.5, result.getDistanceTraveled());
        assertEquals(15.0, result.getBatteryUsed());
        assertNotNull(result.getTotalTime());
        verify(deliveryRepository).save(inProgressDelivery);
    }
    
    @Test
    void testCompleteDelivery_notFound_shouldThrowException() {
        when(deliveryRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            deliveryService.completeDelivery("non-existent", 10.0, 5.0);
        });
    }
    
    @Test
    void testGenerateDeliveryReport_withCompletedDeliveries_shouldCalculateMetrics() {
        Delivery completed1 = Delivery.builder()
            .status(Delivery.DeliveryStatus.COMPLETED)
            .distanceTraveled(10.0)
            .totalTime(15.0)
            .build();
            
        Delivery completed2 = Delivery.builder()
            .status(Delivery.DeliveryStatus.COMPLETED)
            .distanceTraveled(20.0)
            .totalTime(25.0)
            .build();
            
        Delivery failed = Delivery.builder()
            .status(Delivery.DeliveryStatus.FAILED)
            .build();
            
        when(deliveryRepository.findAll())
            .thenReturn(Arrays.asList(completed1, completed2, failed));
        
        when(deliveryRepository.findByStatus(Delivery.DeliveryStatus.COMPLETED))
            .thenReturn(Arrays.asList(completed1, completed2));

        Map<String, Object> report = deliveryService.generateDeliveryReport();

        assertNotNull(report);
        assertEquals(3, report.get("totalDeliveries"));
        assertEquals(2, report.get("completedDeliveries"));
        assertEquals(30.0, report.get("totalDistanceKm"));
        assertEquals(20.0, report.get("averageDeliveryTimeMinutes"));
        assertTrue(((double) report.get("successRate")) > 0);
        assertNotNull(report.get("reportGenerated"));
    }
    
    @Test
    void testGenerateDeliveryReport_withNoDeliveries_shouldHandleGracefully() {
        when(deliveryRepository.findAll()).thenReturn(Arrays.asList());
        when(deliveryRepository.findByStatus(Delivery.DeliveryStatus.COMPLETED))
            .thenReturn(Arrays.asList());

        Map<String, Object> report = deliveryService.generateDeliveryReport();

        assertNotNull(report);
        assertEquals(0, report.get("totalDeliveries"));
        assertEquals(0, report.get("completedDeliveries"));
        assertEquals(0.0, report.get("successRate"));
        assertEquals(0.0, report.get("totalDistanceKm"));
        assertEquals(0.0, report.get("averageDeliveryTimeMinutes"));
    }
}