package com.dti.drone_delivery.service;

import com.dti.drone_delivery.model.Drone;
import com.dti.drone_delivery.model.Order;
import com.dti.drone_delivery.repository.DroneRepository;
import com.dti.drone_delivery.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulationService {
    
    private final WebSocketService webSocketService;
    private final DroneRepository droneRepository;
    private final OrderRepository orderRepository;
    private final Random random = new Random();

    @Scheduled(fixedDelay = 30000)
    public void generateRandomOrders() {
        if (random.nextDouble() > 0.7) {
            Order order = Order.builder()
                .customerName("Cliente " + random.nextInt(1000))
                .locationX(random.nextDouble() * 20 - 10)
                .locationY(random.nextDouble() * 20 - 10)
                .weight(random.nextDouble() * 4 + 1)
                .priority(Order.Priority.values()[random.nextInt(Order.Priority.values().length)])
                .build();
            
            orderRepository.save(order);
            log.info("📦 Pedido gerado automaticamente: {}", order.getCustomerName());
        }
    }
    
    @Scheduled(fixedDelay = 10000)
    public void updateDroneStates() {
        List<Drone> activeDrones = droneRepository.findByStatusIn(
            List.of(Drone.DroneStatus.FLYING, Drone.DroneStatus.DELIVERING, 
                   Drone.DroneStatus.RETURNING)
        );
        
        for (Drone drone : activeDrones) {
            simulateDroneMovement(drone);
        }
    }
    
    @Async
    public void simulateDroneMovement(Drone drone) {
        switch (drone.getStatus()) {
            case FLYING -> moveDroneTowardsTarget(drone);
                
            case DELIVERING -> {
                try {
                    Thread.sleep(5000 + random.nextInt(5000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                drone.setStatus(Drone.DroneStatus.RETURNING);
                droneRepository.save(drone);
                log.info("🚚 Drone {} entregou pacote, retornando à base", drone.getName());
            }
                
            case RETURNING -> returnToBase(drone);
                
            case CHARGING -> rechargeBattery(drone);
            default -> {
            }
        }
    }
    
    private void moveDroneTowardsTarget(Drone drone) {
        double targetX = 5.0;
        double targetY = 5.0;
        
        double dx = targetX - drone.getCurrentX();
        double dy = targetY - drone.getCurrentY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 0.5) {
            drone.setStatus(Drone.DroneStatus.DELIVERING);
        } else {
            drone.setCurrentX(drone.getCurrentX() + dx * 0.1);
            drone.setCurrentY(drone.getCurrentY() + dy * 0.1);

            double batteryUsed = distance * 0.1;
            drone.setCurrentBattery(Math.max(0, drone.getCurrentBattery() - batteryUsed));
            
            if (drone.getCurrentBattery() < 15) {
                drone.setStatus(Drone.DroneStatus.RETURNING);
                log.warn("⚠️ Drone {} com bateria baixa ({}) voltando", 
                        drone.getName(), drone.getCurrentBattery());
            }
        }
        
        drone.setLastUpdate(LocalDateTime.now());
        droneRepository.save(drone);
    }
    
    private void returnToBase(Drone drone) {
        double dx = drone.getBaseX() - drone.getCurrentX();
        double dy = drone.getBaseY() - drone.getCurrentY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 0.3) {
            drone.setCurrentX(drone.getBaseX());
            drone.setCurrentY(drone.getBaseY());
            
            if (drone.getCurrentBattery() < 30) {
                drone.setStatus(Drone.DroneStatus.CHARGING);
            } else {
                drone.setStatus(Drone.DroneStatus.IDLE);
            }
        } else {
            drone.setCurrentX(drone.getCurrentX() + dx * 0.2);
            drone.setCurrentY(drone.getCurrentY() + dy * 0.2);
        }
        
        droneRepository.save(drone);
    }
    
    private void rechargeBattery(Drone drone) {
        double newBattery = Math.min(100, drone.getCurrentBattery() + 15);
        
        drone.setCurrentBattery(newBattery);
        drone.setLastUpdate(LocalDateTime.now());
        
        if (newBattery >= 80) {
            drone.setStatus(Drone.DroneStatus.IDLE);
            log.info("🔋 Drone {} recarregado ({})", drone.getName(), newBattery);
        }
        
        droneRepository.save(drone);
    }
   
    public void initializeSimulation() {
        log.info("🚀 Inicializando simulação...");

        if (droneRepository.count() == 0) {
            for (int i = 1; i <= 5; i++) {
                Drone drone = Drone.builder()
                    .name("Drone-" + i)
                    .maxWeight(10.0)
                    .maxDistance(50.0)
                    .batteryCapacity(100.0)
                    .currentBattery(100.0)
                    .status(Drone.DroneStatus.IDLE)
                    .baseX(0.0)
                    .baseY(0.0)
                    .currentX(0.0)
                    .currentY(0.0)
                    .build();
                droneRepository.save(drone);
            }
            log.info("✅ 5 drones criados");
        }
        
        log.info("✅ Simulação inicializada");
    }

@Scheduled(fixedDelay = 5000)
public void broadcastUpdates() {
    try {
        List<Drone> drones = droneRepository.findAll();
        webSocketService.sendDroneUpdate(drones);

        List<Order> orders = orderRepository.findByStatus(Order.OrderStatus.PENDING);
        webSocketService.sendOrderUpdate(orders);

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("activeDrones", droneRepository.findByStatusIn(
            List.of(Drone.DroneStatus.FLYING, Drone.DroneStatus.DELIVERING)).size());
        metrics.put("pendingOrders", orders.size());
        metrics.put("timestamp", LocalDateTime.now());
        webSocketService.sendMetricsUpdate(metrics);
        
    } catch (Exception e) {
        log.error("Erro ao broadcast WebSocket: {}", e.getMessage());
    }
}
}