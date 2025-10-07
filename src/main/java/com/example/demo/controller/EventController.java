package com.example.demo.controller;

import com.example.demo.model.Event;
import com.example.demo.service.EventService;
import com.example.demo.websocket.EventProgressWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:5173")
public class EventController {

    @Autowired
    private EventService eventService;
    
    @Autowired
    private EventProgressWebSocketHandler webSocketHandler;

    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable String id) {
        try {
            UUID eventId = UUID.fromString(id);
            Event event = eventService.getEventById(eventId);
            return event != null ? ResponseEntity.ok(event) : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        Event createdEvent = eventService.createEvent(event);
        
        // Broadcast real-time creation
        webSocketHandler.broadcastEventCreation(createdEvent);
        
        return ResponseEntity.ok(createdEvent);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable String id, @RequestBody Event eventUpdate) {
        try {
            UUID eventId = UUID.fromString(id);
            Event updatedEvent = eventService.updateEvent(eventId, eventUpdate);
            
            if (updatedEvent != null) {
                // Broadcast real-time update
                webSocketHandler.broadcastEventUpdate(updatedEvent);
                return ResponseEntity.ok(updatedEvent);
            }
            
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String id) {
        try {
            UUID eventId = UUID.fromString(id);
            boolean deleted = eventService.deleteEvent(eventId);
            
            if (deleted) {
                // Broadcast real-time deletion
                webSocketHandler.broadcastEventDeletion(id);
                return ResponseEntity.ok().build();
            }
            
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Test endpoints for WebSocket functionality
    @PostMapping("/test/websocket")
    public ResponseEntity<String> testWebSocket(@RequestParam(defaultValue = "Hello WebSocket!") String message) {
        try {
            webSocketHandler.broadcastSystemStatus("TEST", "WebSocket test message: " + message);
            int connectionCount = webSocketHandler.getActiveConnectionCount();
            return ResponseEntity.ok("WebSocket test message sent to " + connectionCount + " active connections");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send WebSocket test message: " + e.getMessage());
        }
    }
    
    @GetMapping("/test/websocket/ping")
    public ResponseEntity<String> testWebSocketPing() {
        try {
            webSocketHandler.sendPingToAllSessions();
            return ResponseEntity.ok("Ping sent to " + webSocketHandler.getActiveConnectionCount() + " active connections");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error sending ping: " + e.getMessage());
        }
    }

    @GetMapping("/test/websocket/status")
    public ResponseEntity<String> getWebSocketStatus() {
        int activeConnections = webSocketHandler.getActiveConnectionCount();
        return ResponseEntity.ok("Active WebSocket connections: " + activeConnections);
    }

    @PostMapping("/test/websocket/broadcast")
    public ResponseEntity<String> testBroadcast(@RequestParam String message) {
        try {
            webSocketHandler.broadcastSystemStatus("TEST", message);
            return ResponseEntity.ok("Test message broadcasted to " + webSocketHandler.getActiveConnectionCount() + " connections");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error broadcasting: " + e.getMessage());
        }
    }
    
    // Test endpoint to manually trigger task update broadcast
    @PostMapping("/test-task-update/{eventId}")
    public ResponseEntity<String> testTaskUpdateBroadcast(@PathVariable String eventId, @RequestParam(defaultValue = "Test Task") String taskTitle) {
        try {
            // Create a dummy task object for testing
            Map<String, Object> testTask = new HashMap<>();
            testTask.put("id", "test-task-" + System.currentTimeMillis());
            testTask.put("title", taskTitle);
            testTask.put("description", "This is a test task update");
            testTask.put("completed", false);
            
            webSocketHandler.broadcastTaskUpdate(eventId, testTask);
            return ResponseEntity.ok("Test task update broadcasted for event: " + eventId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error broadcasting test task update: " + e.getMessage());
        }
    }

    // Utility endpoint to clean up duplicate events (for demo purposes)
    @DeleteMapping("/cleanup-duplicates")
    public ResponseEntity<String> cleanupDuplicateEvents() {
        try {
            List<Event> allEvents = eventService.getAllEvents();
            Map<String, List<Event>> eventsByNameAndDate = allEvents.stream()
                .collect(Collectors.groupingBy(event -> event.getName() + "_" + event.getDate()));
            
            int deletedCount = 0;
            for (Map.Entry<String, List<Event>> entry : eventsByNameAndDate.entrySet()) {
                List<Event> duplicates = entry.getValue();
                if (duplicates.size() > 1) {
                    // Keep the first one, delete the rest
                    for (int i = 1; i < duplicates.size(); i++) {
                        eventService.deleteEvent(duplicates.get(i).getId());
                        deletedCount++;
                    }
                }
            }
            
            return ResponseEntity.ok("Cleaned up " + deletedCount + " duplicate events");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to cleanup duplicates: " + e.getMessage());
        }
    }
}
