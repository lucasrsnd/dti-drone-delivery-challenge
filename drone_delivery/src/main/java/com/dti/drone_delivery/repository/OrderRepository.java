package com.dti.drone_delivery.repository;

import com.dti.drone_delivery.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    List<Order> findByStatus(Order.OrderStatus status);
    
    List<Order> findByPriority(Order.Priority priority);
    
    List<Order> findByStatusAndPriorityOrderByCreatedAtAsc(
        Order.OrderStatus status, 
        Order.Priority priority
    );
    
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.weight <= :maxWeight ORDER BY o.priority DESC, o.createdAt ASC")
    List<Order> findPendingOrdersByMaxWeight(Double maxWeight);
    
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<Order> findByAssignedDroneId(String droneId);
    
    long countByStatus(Order.OrderStatus status);
}