package com.dti.drone_delivery.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.dti.drone_delivery.dto.OrderRequest;
import com.dti.drone_delivery.model.Order;
import com.dti.drone_delivery.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @InjectMocks
    private OrderService orderService;
    
    private Order order;
    private OrderRequest orderRequest;
    
    @BeforeEach
    void setUp() {
        order = Order.builder()
            .id("order-test-1")
            .customerName("John Doe")
            .locationX(10.0)
            .locationY(20.0)
            .weight(5.0)
            .priority(Order.Priority.MEDIUM)
            .status(Order.OrderStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();
            
        orderRequest = new OrderRequest();
        orderRequest.setCustomerName("Jane Doe");
        orderRequest.setLocationX(15.0);
        orderRequest.setLocationY(25.0);
        orderRequest.setWeight(3.0);
        orderRequest.setPriority(Order.Priority.HIGH);
    }
    
    @Test
    void testCreateOrder_shouldSaveAndReturnOrder() {
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.createOrder(orderRequest);

        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
        assertEquals(Order.OrderStatus.PENDING, result.getStatus());
        verify(orderRepository).save(any(Order.class));
    }
    
    @Test
    void testGetAllOrders_shouldReturnAllOrders() {
        Order order2 = Order.builder().id("order-test-2").build();
        when(orderRepository.findAll()).thenReturn(Arrays.asList(order, order2));

        List<Order> result = orderService.getAllOrders();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository).findAll();
    }
    
    @Test
    void testGetPendingOrders_shouldReturnOnlyPending() {
        Order pendingOrder = Order.builder()
            .status(Order.OrderStatus.PENDING)
            .build();
            
        when(orderRepository.findByStatus(Order.OrderStatus.PENDING))
            .thenReturn(Arrays.asList(pendingOrder));

        List<Order> result = orderService.getPendingOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Order.OrderStatus.PENDING, result.get(0).getStatus());
        verify(orderRepository).findByStatus(Order.OrderStatus.PENDING);
    }
    
    @Test
    void testUpdateOrderStatus_toDelivered_shouldSetDeliveredAt() {

        when(orderRepository.findById("order-test-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.updateOrderStatus("order-test-1", Order.OrderStatus.DELIVERED);

        assertNotNull(result);
        assertEquals(Order.OrderStatus.DELIVERED, result.getStatus());
        assertNotNull(result.getDeliveredAt());
        verify(orderRepository).save(order);
    }
    
    @Test
    void testUpdateOrderStatus_notFound_shouldThrowException() {
        when(orderRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            orderService.updateOrderStatus("non-existent", Order.OrderStatus.DELIVERED);
        });
    }
    
    @Test
    void testCancelOrder_shouldSetStatusToCancelled() {

        when(orderRepository.findById("order-test-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.cancelOrder("order-test-1");
        
        assertEquals(Order.OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository).save(order);
    }
    
    @Test
    void testCountPendingOrders_shouldReturnCount() {

        when(orderRepository.countByStatus(Order.OrderStatus.PENDING)).thenReturn(5L);

        long count = orderService.countPendingOrders();

        assertEquals(5L, count);
        verify(orderRepository).countByStatus(Order.OrderStatus.PENDING);
    }
    
    @Test
    void testGetOrdersByDrone_shouldReturnOrders() {
        Order assignedOrder = Order.builder()
            .assignedDroneId("drone-1")
            .build();
            
        when(orderRepository.findByAssignedDroneId("drone-1"))
            .thenReturn(Arrays.asList(assignedOrder));

        List<Order> result = orderService.getOrdersByDrone("drone-1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("drone-1", result.get(0).getAssignedDroneId());
        verify(orderRepository).findByAssignedDroneId("drone-1");
    }
}