package com.dti.drone_delivery.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.dti.drone_delivery.model.Drone;
import com.dti.drone_delivery.model.Order;
import com.dti.drone_delivery.repository.DroneRepository;
import com.dti.drone_delivery.repository.OrderRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulationService {
    
    private final DroneRepository droneRepository;
    private final OrderRepository orderRepository;
    private final AllocationService allocationService;
    private ScheduledExecutorService scheduler;
    
    @PostConstruct
    public void init() {
        log.info("🚀 Iniciando simulador de drones...");
        
        // Cria alguns drones iniciais se não existirem
        if (droneRepository.count() == 0) {
            createInitialDrones();
        }
        
        // Inicia o scheduler manual (mais confiável que @Scheduled)
        scheduler = Executors.newScheduledThreadPool(2);
        
        // Agendamento 1: Atualizar drones a cada 3 segundos
        scheduler.scheduleAtFixedRate(() -> {
            try {
                updateDroneStates();
            } catch (Exception e) {
                log.error("Erro ao atualizar drones: {}", e.getMessage());
            }
        }, 0, 3, TimeUnit.SECONDS);
        
        // Agendamento 2: Gerar pedidos aleatórios a cada 10 segundos
        scheduler.scheduleAtFixedRate(() -> {
            try {
                generateRandomOrder();
            } catch (Exception e) {
                log.error("Erro ao gerar pedido: {}", e.getMessage());
            }
        }, 5, 10, TimeUnit.SECONDS);
        
        // Agendamento 3: Alocar pedidos a cada 5 segundos
        scheduler.scheduleAtFixedRate(() -> {
            try {
                allocationService.allocateOrdersToDrones();
            } catch (Exception e) {
                log.error("Erro na alocação: {}", e.getMessage());
            }
        }, 2, 5, TimeUnit.SECONDS);
        
        log.info("✅ Simulador iniciado com 3 tarefas agendadas");
    }
    
    private void createInitialDrones() {
        for (int i = 1; i <= 3; i++) {
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
        log.info("✅ 3 drones iniciais criados");
    }
    
    public void generateRandomOrder() {
        Random random = new Random();
        
        if (random.nextDouble() > 0.3) { // 70% de chance
            Order order = Order.builder()
                .customerName("Cliente-" + random.nextInt(1000))
                .locationX(random.nextDouble() * 20 - 10) // -10 a 10
                .locationY(random.nextDouble() * 20 - 10)
                .weight(random.nextDouble() * 4 + 1) // 1-5 kg
                .priority(Order.Priority.values()[random.nextInt(Order.Priority.values().length)])
                .build();
            
            orderRepository.save(order);
            log.info("📦 Pedido gerado: {} ({}kg)", order.getCustomerName(), String.format("%.1f", order.getWeight()));
        }
    }
    
    public void updateDroneStates() {
        List<Drone> allDrones = droneRepository.findAll();
        
        for (Drone drone : allDrones) {
            switch (drone.getStatus()) {
                case LOADING:
                    // Após carregar, vai para FLYING
                    drone.setStatus(Drone.DroneStatus.FLYING);
                    log.info("✈️ {} carregou, agora está VOANDO", drone.getName());
                    break;
                    
                case FLYING:
                    // Move o drone
                    moveDroneToDestination(drone);
                    break;
                    
                case DELIVERING:
                    // Entrega os pedidos
                    deliverOrders(drone);
                    break;
                    
                case RETURNING:
                    // Retorna à base
                    returnToBase(drone);
                    break;
                    
                case CHARGING:
                    // Recarrega bateria
                    rechargeBattery(drone);
                    break;
            }
            
            drone.setLastUpdate(LocalDateTime.now());
            droneRepository.save(drone);
        }
    }
    
    private void moveDroneToDestination(Drone drone) {
        // Se não tem destino, escolhe um pedido aleatório
        if (Math.abs(drone.getCurrentX() - drone.getBaseX()) < 0.1 && 
            Math.abs(drone.getCurrentY() - drone.getBaseY()) < 0.1) {
            
            List<Order> assignedOrders = orderRepository.findByAssignedDroneId(drone.getId());
            if (!assignedOrders.isEmpty()) {
                Order targetOrder = assignedOrders.get(0);
                // Define destino como localização do pedido
                drone.setCurrentX(targetOrder.getLocationX());
                drone.setCurrentY(targetOrder.getLocationY());
                log.info("🎯 {} indo para pedido em ({}, {})", 
                    drone.getName(), 
                    String.format("%.1f", targetOrder.getLocationX()),
                    String.format("%.1f", targetOrder.getLocationY()));
            }
        }
        
        // Consome bateria
        drone.setCurrentBattery(Math.max(0, drone.getCurrentBattery() - 0.5));
        
        // Se chegou perto do destino
        if (Math.abs(drone.getCurrentX() - drone.getBaseX()) > 1 || 
            Math.abs(drone.getCurrentY() - drone.getBaseY()) > 1) {
            
            if (drone.getCurrentBattery() < 20) {
                drone.setStatus(Drone.DroneStatus.RETURNING);
                log.info("🔋 {} com bateria baixa, RETORNANDO", drone.getName());
            } else {
                drone.setStatus(Drone.DroneStatus.DELIVERING);
                log.info("📦 {} chegou ao destino, ENTREGANDO", drone.getName());
            }
        }
    }
    
    private void deliverOrders(Drone drone) {
        List<Order> assignedOrders = orderRepository.findByAssignedDroneId(drone.getId());
        
        for (Order order : assignedOrders) {
            order.setStatus(Order.OrderStatus.DELIVERED);
            order.setDeliveredAt(LocalDateTime.now());
            orderRepository.save(order);
            log.info("✅ {} ENTREGOU pedido para {}", drone.getName(), order.getCustomerName());
        }
        
        drone.setStatus(Drone.DroneStatus.RETURNING);
    }
    
    private void returnToBase(Drone drone) {
        // Move de volta para a base (0,0)
        double dx = drone.getBaseX() - drone.getCurrentX();
        double dy = drone.getBaseY() - drone.getCurrentY();
        
        if (Math.abs(dx) > 0.1) {
            drone.setCurrentX(drone.getCurrentX() + dx * 0.5);
        }
        if (Math.abs(dy) > 0.1) {
            drone.setCurrentY(drone.getCurrentY() + dy * 0.5);
        }
        
        // Consome bateria
        drone.setCurrentBattery(Math.max(0, drone.getCurrentBattery() - 0.3));
        
        // Se chegou na base
        if (Math.abs(dx) < 0.2 && Math.abs(dy) < 0.2) {
            drone.setCurrentX(drone.getBaseX());
            drone.setCurrentY(drone.getBaseY());
            
            if (drone.getCurrentBattery() < 30) {
                drone.setStatus(Drone.DroneStatus.CHARGING);
                log.info("🔌 {} na base, CARREGANDO", drone.getName());
            } else {
                drone.setStatus(Drone.DroneStatus.IDLE);
                log.info("🏠 {} na base, OCIOSO", drone.getName());
            }
        }
    }
    
    private void rechargeBattery(Drone drone) {
        double newBattery = Math.min(100, drone.getCurrentBattery() + 15);
        drone.setCurrentBattery(newBattery);
        
        if (newBattery >= 80) {
            drone.setStatus(Drone.DroneStatus.IDLE);
            log.info("⚡ {} carregado ({}%), OCIOSO", drone.getName(), String.format("%.0f", newBattery));
        }
    }
    
    // Método para resetar simulação
    public void resetSimulation() {
        log.info("🔄 Resetando simulação...");
        
        // Remove todos os pedidos
        orderRepository.deleteAll();
        
        // Reseta todos os drones
        List<Drone> drones = droneRepository.findAll();
        for (Drone drone : drones) {
            drone.setStatus(Drone.DroneStatus.IDLE);
            drone.setCurrentBattery(100.0);
            drone.setCurrentX(0.0);
            drone.setCurrentY(0.0);
            drone.setLastUpdate(LocalDateTime.now());
            droneRepository.save(drone);
        }
        
        log.info("✅ Simulação resetada");
    }
}