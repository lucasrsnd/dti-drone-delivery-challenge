package com.dti.drone_delivery.service;

import com.dti.drone_delivery.model.Delivery;
import com.dti.drone_delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    
    private final DeliveryRepository deliveryRepository;
    
    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }
    
    public List<Delivery> getDeliveriesByDrone(String droneId) {
        return deliveryRepository.findByDroneId(droneId);
    }
    
    public List<Delivery> getDeliveriesByStatus(Delivery.DeliveryStatus status) {
        return deliveryRepository.findByStatus(status);
    }
    
    public Delivery createDelivery(String orderId, String droneId) {
        Delivery delivery = Delivery.builder()
            .orderId(orderId)
            .droneId(droneId)
            .status(Delivery.DeliveryStatus.IN_PROGRESS)
            .startedAt(LocalDateTime.now())
            .build();
        
        return deliveryRepository.save(delivery);
    }
    
    public Delivery completeDelivery(String deliveryId, double distance, double batteryUsed) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new RuntimeException("Entrega não encontrada"));
        
        delivery.setStatus(Delivery.DeliveryStatus.COMPLETED);
        delivery.setDeliveredAt(LocalDateTime.now());
        delivery.setDistanceTraveled(distance);
        delivery.setBatteryUsed(batteryUsed);
        
        if (delivery.getStartedAt() != null) {
            long minutes = java.time.Duration.between(
                delivery.getStartedAt(), 
                delivery.getDeliveredAt()
            ).toMinutes();
            delivery.setTotalTime((double) minutes);
        }
        
        return deliveryRepository.save(delivery);
    }
    
    public Map<String, Object> generateDeliveryReport() {
        List<Delivery> deliveries = getAllDeliveries();
        List<Delivery> completed = getDeliveriesByStatus(Delivery.DeliveryStatus.COMPLETED);
        
        Map<String, Object> report = new HashMap<>();
        report.put("totalDeliveries", deliveries.size());
        report.put("completedDeliveries", completed.size());
        report.put("successRate", deliveries.isEmpty() ? 0 : 
            (double) completed.size() / deliveries.size() * 100);
        
        // Estatísticas de tempo
        double totalTime = completed.stream()
            .filter(d -> d.getTotalTime() != null)
            .mapToDouble(Delivery::getTotalTime)
            .sum();
        report.put("totalDeliveryTimeMinutes", totalTime);
        report.put("averageDeliveryTimeMinutes", 
            completed.isEmpty() ? 0 : totalTime / completed.size());

        double totalDistance = completed.stream()
            .filter(d -> d.getDistanceTraveled() != null)
            .mapToDouble(Delivery::getDistanceTraveled)
            .sum();
        report.put("totalDistanceKm", totalDistance);
        
        report.put("reportGenerated", LocalDateTime.now());
        
        return report;
    }
}