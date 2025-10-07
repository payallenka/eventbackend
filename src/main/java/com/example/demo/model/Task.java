package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Task {
    @Id
    @GeneratedValue
    private UUID id;

    private String title;
    private String description;
    private boolean completed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Event event;

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
}
