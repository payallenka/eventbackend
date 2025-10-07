package com.example.demo.controller;

import com.example.demo.model.Attendee;
import com.example.demo.service.AttendeeService;
import com.example.demo.websocket.EventProgressWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events/{eventId}/attendees")
@CrossOrigin(origins = "http://localhost:5173")
public class AttendeeController {

    @Autowired
    private AttendeeService attendeeService;
    
    @Autowired
    private EventProgressWebSocketHandler webSocketHandler;

    @GetMapping
    public List<Attendee> getAttendeesForEvent(@PathVariable String eventId) {
        return attendeeService.getAttendeesForEvent(eventId);
    }

    @PostMapping
    public ResponseEntity<Attendee> createAttendee(@PathVariable String eventId, @RequestBody Attendee attendee) {
        Attendee createdAttendee = attendeeService.createAttendee(eventId, attendee);
        
        // Broadcast real-time creation
        webSocketHandler.broadcastAttendeeCreation(eventId, createdAttendee);
        
        return ResponseEntity.ok(createdAttendee);
    }

    @PutMapping("/{attendeeId}")
    public ResponseEntity<Attendee> updateAttendee(
            @PathVariable String eventId,
            @PathVariable String attendeeId, 
            @RequestBody Attendee attendeeUpdate) {
        
        Attendee updatedAttendee = attendeeService.updateAttendee(eventId, attendeeId, attendeeUpdate);
        
        if (updatedAttendee != null) {
            // Broadcast real-time update
            webSocketHandler.broadcastAttendeeUpdate(eventId, updatedAttendee);
            return ResponseEntity.ok(updatedAttendee);
        }
        
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{attendeeId}")
    public ResponseEntity<Void> deleteAttendee(@PathVariable String eventId, @PathVariable String attendeeId) {
        boolean deleted = attendeeService.deleteAttendee(eventId, attendeeId);
        
        if (deleted) {
            // Broadcast real-time deletion - send the attendeeId string for frontend compatibility
            webSocketHandler.broadcastAttendeeDeletion(eventId, attendeeId);
            return ResponseEntity.ok().build();
        }
        
        return ResponseEntity.notFound().build();
    }
}
