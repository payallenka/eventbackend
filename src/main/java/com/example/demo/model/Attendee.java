package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Attendee {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Event event;

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
}
