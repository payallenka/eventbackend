package com.example.demo.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EventProgressWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper;
    
    public EventProgressWebSocketHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        sessions.remove(session);
    }

    // Task-related broadcasts
    public void broadcastTaskUpdate(String eventId, Object taskData) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "TASK_UPDATE");
        message.put("eventId", eventId);
        message.put("data", taskData);
        broadcastMessage(message);
    }

    public void broadcastTaskCreation(String eventId, Object taskData) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "TASK_CREATE");
        message.put("eventId", eventId);
        message.put("data", taskData);
        broadcastMessage(message);
    }

    public void broadcastTaskDeletion(String eventId, Object taskId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "TASK_DELETE");
        message.put("eventId", eventId);
        message.put("taskId", taskId);
        broadcastMessage(message);
    }

    // Event-related broadcasts
    public void broadcastEventUpdate(Object eventData) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "EVENT_UPDATE");
        message.put("data", eventData);
        broadcastMessage(message);
    }

    public void broadcastEventCreation(Object eventData) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "EVENT_CREATE");
        message.put("data", eventData);
        broadcastMessage(message);
    }

    public void broadcastEventDeletion(String eventId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "EVENT_DELETE");
        message.put("eventId", eventId);
        broadcastMessage(message);
    }

    // Attendee-related broadcasts
    public void broadcastAttendeeUpdate(String eventId, Object attendeeData) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "ATTENDEE_UPDATE");
        message.put("eventId", eventId);
        message.put("data", attendeeData);
        broadcastMessage(message);
    }

    public void broadcastAttendeeCreation(String eventId, Object attendeeData) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "ATTENDEE_CREATE");
        message.put("eventId", eventId);
        message.put("data", attendeeData);
        broadcastMessage(message);
    }

    public void broadcastAttendeeDeletion(String eventId, Object attendeeId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "ATTENDEE_DELETE");
        message.put("eventId", eventId);
        message.put("attendeeId", attendeeId);
        broadcastMessage(message);
    }

    // Utility method for testing WebSocket connectivity
    public void sendPingToAllSessions() {
        Map<String, Object> pingMessage = new HashMap<>();
        pingMessage.put("type", "PING");
        pingMessage.put("timestamp", System.currentTimeMillis());
        pingMessage.put("activeConnections", sessions.size());
        broadcastMessage(pingMessage);
    }

    // Get current connection count
    public int getActiveConnectionCount() {
        return sessions.size();
    }

    // Method to broadcast a simple status update
    public void broadcastSystemStatus(String status, String message) {
        Map<String, Object> statusMessage = new HashMap<>();
        statusMessage.put("type", "SYSTEM_STATUS");
        statusMessage.put("status", status);
        statusMessage.put("message", message);
        statusMessage.put("timestamp", System.currentTimeMillis());
        statusMessage.put("activeConnections", sessions.size());
        broadcastMessage(statusMessage);
    }

    private void broadcastMessage(Map<String, Object> message) {
        String messageJson = createMessageJson(message);
        
        sessions.removeIf(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(messageJson));
                    return false;
                } else {
                    return true;
                }
            } catch (Exception e) {
                return true;
            }
        });
    }

    private String createMessageJson(Map<String, Object> message) {
        try {
            Map<String, Object> safeMessage = new HashMap<>(message);
            
            Object data = safeMessage.get("data");
            if (data != null) {
                Object simplifiedData = createSimplifiedEntity(data);
                safeMessage.put("data", simplifiedData);
            }
            
            return objectMapper.writeValueAsString(safeMessage);
        } catch (Exception e) {
            Map<String, Object> errorMessage = new HashMap<>();
            errorMessage.put("type", message.get("type"));
            errorMessage.put("eventId", message.get("eventId"));
            errorMessage.put("error", "Serialization failed");
            errorMessage.put("timestamp", System.currentTimeMillis());
            
            Object taskId = message.get("taskId");
            Object attendeeId = message.get("attendeeId");
            if (taskId != null) errorMessage.put("taskId", taskId);
            if (attendeeId != null) errorMessage.put("attendeeId", attendeeId);
            
            try {
                return objectMapper.writeValueAsString(errorMessage);
            } catch (Exception ex) {
                return String.format("{\"error\":\"Serialization failure\",\"type\":\"%s\",\"timestamp\":%d}", 
                    message.get("type"), System.currentTimeMillis());
            }
        }
    }
    
    private Object createSimplifiedEntity(Object entity) {
        if (entity == null || entity instanceof String || entity instanceof Number || entity instanceof Boolean) {
            return entity;
        }
        
        try {
            Map<String, Object> simplified = new HashMap<>();
            String entityType = entity.getClass().getSimpleName();
            
            switch (entityType) {
                case "Task":
                    simplified.put("id", entity.getClass().getMethod("getId").invoke(entity));
                    simplified.put("title", entity.getClass().getMethod("getTitle").invoke(entity));
                    simplified.put("description", entity.getClass().getMethod("getDescription").invoke(entity));
                    simplified.put("completed", entity.getClass().getMethod("isCompleted").invoke(entity));
                    break;
                case "Event":
                    simplified.put("id", entity.getClass().getMethod("getId").invoke(entity));
                    simplified.put("name", entity.getClass().getMethod("getName").invoke(entity));
                    simplified.put("date", entity.getClass().getMethod("getDate").invoke(entity));
                    simplified.put("description", entity.getClass().getMethod("getDescription").invoke(entity));
                    simplified.put("location", entity.getClass().getMethod("getLocation").invoke(entity));
                    break;
                case "Attendee":
                    simplified.put("id", entity.getClass().getMethod("getId").invoke(entity));
                    simplified.put("name", entity.getClass().getMethod("getName").invoke(entity));
                    simplified.put("email", entity.getClass().getMethod("getEmail").invoke(entity));
                    break;
                default:
                    simplified.put("updated", true);
                    simplified.put("timestamp", System.currentTimeMillis());
                    simplified.put("entityType", entityType);
            }
            
            return simplified;
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("updated", true);
            fallback.put("timestamp", System.currentTimeMillis());
            return fallback;
        }
    }
}
