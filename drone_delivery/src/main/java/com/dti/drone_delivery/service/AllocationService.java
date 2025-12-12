package com.dti.drone_delivery.service;

import com.dti.drone_delivery.model.Drone;
import com.dti.drone_delivery.model.Order;
import com.dti.drone_delivery.repository.DroneRepository;
import com.dti.drone_delivery.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AllocationService {
    
    private final DroneRepository droneRepository;
    private final OrderRepository orderRepository;
    private final ObstacleService obstacleService;
    
    public List<Order> optimizePackagesForDrone(Drone drone, List<Order> orders) {
        int capacity = drone.getMaxWeight().intValue();
        int n = orders.size();

        double[][] dp = new double[n + 1][capacity + 1];
        boolean[][] keep = new boolean[n + 1][capacity + 1];

        for (int i = 1; i <= n; i++) {
            Order order = orders.get(i - 1);
            int weight = order.getWeight().intValue();
            double value = calculateOrderValue(order);
            
            for (int w = 1; w <= capacity; w++) {
                if (weight <= w && dp[i - 1][w - weight] + value > dp[i - 1][w]) {
                    dp[i][w] = dp[i - 1][w - weight] + value;
                    keep[i][w] = true;
                } else {
                    dp[i][w] = dp[i - 1][w];
                    keep[i][w] = false;
                }
            }
        }

        List<Order> selectedOrders = new ArrayList<>();
        int w = capacity;
        for (int i = n; i > 0; i--) {
            if (keep[i][w]) {
                selectedOrders.add(orders.get(i - 1));
                w -= orders.get(i - 1).getWeight().intValue();
            }
        }
        
        log.info("Knapsack: {} pedidos otimizados para drone {}", selectedOrders.size(), drone.getName());
        return selectedOrders;
    }

    private double calculateOrderValue(Order order) {
    double priorityValue;
    priorityValue = switch (order.getPriority()) {
            case URGENT -> 4.0;
            case HIGH -> 3.0;
            case MEDIUM -> 2.0;
            case LOW -> 1.0;
            default -> 1.0;
        };

    long minutesWaiting = java.time.Duration.between(
        order.getCreatedAt(), 
        java.time.LocalDateTime.now()
    ).toMinutes();
    
    double timeValue = Math.min(5.0, minutesWaiting * 0.01);
    
    return priorityValue + timeValue;
}

    public Map<String, List<Order>> allocateOrdersToDrones() {
        List<Drone> availableDrones = droneRepository.findAvailableDrones(20.0);
        List<Order> pendingOrders = orderRepository.findByStatus(Order.OrderStatus.PENDING);
        
        Map<String, List<Order>> allocation = new HashMap<>();
        
        if (availableDrones.isEmpty() || pendingOrders.isEmpty()) {
            log.info("Nenhum drone disponível ou pedidos pendentes");
            return allocation;
        }

        availableDrones.sort((d1, d2) -> 
            d2.getCurrentBattery().compareTo(d1.getCurrentBattery()));

        pendingOrders.sort((o1, o2) -> {
            int priorityCompare = o2.getPriority().compareTo(o1.getPriority());
            if (priorityCompare != 0) return priorityCompare;
            return o1.getCreatedAt().compareTo(o2.getCreatedAt());
        });

        for (Drone drone : availableDrones) {
            if (pendingOrders.isEmpty()) break;

            List<Order> feasibleOrders = new ArrayList<>();
            for (Order order : pendingOrders) {
                if (order.getWeight() <= drone.getMaxWeight()) {
                    feasibleOrders.add(order);
                }
            }
            
            if (!feasibleOrders.isEmpty()) {
                List<Order> optimized = optimizePackagesForDrone(drone, feasibleOrders);
                
                if (!optimized.isEmpty()) {
                    allocation.put(drone.getId(), optimized);
                    pendingOrders.removeAll(optimized);

                    optimized.forEach(order -> {
                        order.setStatus(Order.OrderStatus.ASSIGNED);
                        order.setAssignedDroneId(drone.getId());
                        orderRepository.save(order);
                    });

                    drone.setStatus(Drone.DroneStatus.LOADING);
                    droneRepository.save(drone);
                    
                    log.info("Drone {} recebeu {} pedidos", drone.getName(), optimized.size());
                }
            }
        }
        
        log.info("Alocação concluída: {} drones receberam pedidos", allocation.size());
        return allocation;
    }

    public List<double[]> calculateRouteWithObstacles(double startX, double startY, 
                                                     double endX, double endY) {
        List<double[]> route = new ArrayList<>();
        route.add(new double[]{startX, startY});

        if (!obstacleService.hasObstacleBetween(startX, startY, endX, endY)) {
            route.add(new double[]{endX, endY});
            return route;
        }

        double midX = (startX + endX) / 2;
        double midY = (startY + endY) / 2 + 1.0;
        
        route.add(new double[]{midX, midY});
        route.add(new double[]{endX, endY});
        
        return route;
    }

    public double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    
    public double calculateBatteryUsage(double distance, double batteryEfficiency) {
        return distance * batteryEfficiency;
    }
}