package com.dti.drone_delivery.service;

import org.springframework.messaging.MessagingException;

import com.dti.drone_delivery.dto.WebSocketMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public void sendDroneUpdate(Object droneData) {
        sendToTopic("/topic/drones", WebSocketMessage.create("DRONE_UPDATE", droneData));
    }
    
    public void sendOrderUpdate(Object orderData) {
        sendToTopic("/topic/orders", WebSocketMessage.create("ORDER_CREATED", orderData));
    }
    
    public void sendDeliveryUpdate(Object deliveryData) {
        sendToTopic("/topic/deliveries", WebSocketMessage.create("DELIVERY_COMPLETED", deliveryData));
    }
    
    public void sendSimulationUpdate(Object simulationData) {
        sendToTopic("/topic/simulation", WebSocketMessage.create("SIMULATION_UPDATE", simulationData));
    }
    
    public void sendMetricsUpdate(Object metricsData) {
        sendToTopic("/topic/metrics", WebSocketMessage.create("METRICS_UPDATE", metricsData));
    }
    
    private void sendToTopic(String topic, WebSocketMessage message) {
        try {
            messagingTemplate.convertAndSend(topic, message);
            log.debug("📡 WebSocket enviado para {}: {}", topic, message.getType());
        } catch (MessagingException e) {
            log.error("Erro ao enviar WebSocket para {}: {}", topic, e.getMessage());
        }
    }
}