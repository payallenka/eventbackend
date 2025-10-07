package com.example.demo.repository;

import com.example.demo.model.Event;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, UUID> {
}
