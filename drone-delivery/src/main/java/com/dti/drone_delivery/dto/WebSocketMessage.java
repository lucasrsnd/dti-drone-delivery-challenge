package com.dti.drone_delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    private String type;
    private Object data;
    private LocalDateTime timestamp;
    
    public static WebSocketMessage create(String type, Object data) {
        return new WebSocketMessage(type, data, LocalDateTime.now());
    }
}