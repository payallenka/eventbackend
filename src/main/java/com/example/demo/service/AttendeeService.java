package com.example.demo.service;

import com.example.demo.model.Attendee;
import com.example.demo.model.Event;
import com.example.demo.repository.AttendeeRepository;
import com.example.demo.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AttendeeService {
    @Autowired
    private AttendeeRepository attendeeRepository;
    @Autowired
    private EventRepository eventRepository;

    public List<Attendee> getAttendeesByEvent(UUID eventId) {
        return attendeeRepository.findByEventId(eventId);
    }

    // Method for controller compatibility
    public List<Attendee> getAttendeesForEvent(String eventId) {
        try {
            UUID uuid = UUID.fromString(eventId);
            return getAttendeesByEvent(uuid);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid event ID format");
        }
    }

    // Method for controller compatibility with Long
    public List<Attendee> getAttendeesForEvent(Long eventId) {
        return getAttendeesForEvent(eventId.toString());
    }

    public Attendee addAttendee(UUID eventId, Attendee attendee) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isPresent()) {
            attendee.setEvent(eventOpt.get());
            return attendeeRepository.save(attendee);
        }
        throw new RuntimeException("Event not found");
    }

    // Method for controller compatibility
    public Attendee createAttendee(String eventId, Attendee attendee) {
        try {
            UUID uuid = UUID.fromString(eventId);
            return addAttendee(uuid, attendee);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid event ID format");
        }
    }

    // Method for controller compatibility with Long
    public Attendee createAttendee(Long eventId, Attendee attendee) {
        return createAttendee(eventId.toString(), attendee);
    }

    public void deleteAttendee(UUID attendeeId) {
        attendeeRepository.deleteById(attendeeId);
    }

    // Method for controller compatibility
    public boolean deleteAttendee(String eventId, String attendeeId) {
        try {
            UUID uuid = UUID.fromString(attendeeId);
            deleteAttendee(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid attendee ID format");
        }
    }

    // Method for controller compatibility with Long
    public boolean deleteAttendee(Long eventId, Long attendeeId) {
        return deleteAttendee(eventId.toString(), attendeeId.toString());
    }

    public Attendee updateAttendee(UUID attendeeId, Attendee updated) {
        Attendee attendee = attendeeRepository.findById(attendeeId)
                .orElseThrow(() -> new RuntimeException("Attendee not found"));
        attendee.setName(updated.getName());
        attendee.setEmail(updated.getEmail());
        return attendeeRepository.save(attendee);
    }

    // Method for controller compatibility
    public Attendee updateAttendee(String eventId, String attendeeId, Attendee updated) {
        try {
            UUID uuid = UUID.fromString(attendeeId);
            return updateAttendee(uuid, updated);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid attendee ID format");
        }
    }

    // Method for controller compatibility with Long
    public Attendee updateAttendee(Long eventId, Long attendeeId, Attendee updated) {
        return updateAttendee(eventId.toString(), attendeeId.toString(), updated);
    }
}
