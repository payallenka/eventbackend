package com.example.demo.service;

import com.example.demo.model.Task;
import com.example.demo.model.Event;
import com.example.demo.model.Attendee;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.AttendeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private AttendeeRepository attendeeRepository;

    public List<Task> getTasksByEvent(UUID eventId) {
        return taskRepository.findByEventId(eventId);
    }

    // Method for controller compatibility
    public List<Task> getTasksForEvent(String eventId) {
        try {
            UUID uuid = UUID.fromString(eventId);
            return getTasksByEvent(uuid);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid event ID format");
        }
    }

    // Method for controller compatibility with Long
    public List<Task> getTasksForEvent(Long eventId) {
        return getTasksForEvent(eventId.toString());
    }

    public Task addTask(UUID eventId, Task task) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isPresent()) {
            task.setEvent(eventOpt.get());
            // Set deadline if present
            if (task.getDeadline() != null) {
                task.setDeadline(task.getDeadline());
            }
            // Set assignedAttendee if present
            if (task.getAssignedAttendee() != null && task.getAssignedAttendee().getId() != null) {
                attendeeRepository.findById(task.getAssignedAttendee().getId()).ifPresent(task::setAssignedAttendee);
            } else {
                task.setAssignedAttendee(null);
            }
            return taskRepository.save(task);
        }
        throw new RuntimeException("Event not found");
    }

    // Method for controller compatibility
    public Task createTask(String eventId, Task task) {
        try {
            UUID uuid = UUID.fromString(eventId);
            return addTask(uuid, task);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid event ID format");
        }
    }

    // Method for controller compatibility with Long
    public Task createTask(Long eventId, Task task) {
        return createTask(eventId.toString(), task);
    }

    public void deleteTask(UUID taskId) {
        taskRepository.deleteById(taskId);
    }

    // Method for controller compatibility
    public void deleteTask(String eventId, String taskId) {
        try {
            UUID uuid = UUID.fromString(taskId);
            deleteTask(uuid);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid task ID format");
        }
    }

    // Method for controller compatibility with Long
    public void deleteTask(Long eventId, Long taskId) {
        deleteTask(eventId.toString(), taskId.toString());
    }

    public Task updateTask(UUID taskId, Task updated) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        // Only update fields that are not null to preserve existing data
        if (updated.getTitle() != null) {
            task.setTitle(updated.getTitle());
        }
        if (updated.getDescription() != null) {
            task.setDescription(updated.getDescription());
        }
        // Always update completed status as it's a boolean
        task.setCompleted(updated.isCompleted());
        // Update deadline if present
        if (updated.getDeadline() != null) {
            task.setDeadline(updated.getDeadline());
        }
        // Update assignedAttendee if present
        if (updated.getAssignedAttendee() != null && updated.getAssignedAttendee().getId() != null) {
            attendeeRepository.findById(updated.getAssignedAttendee().getId()).ifPresent(task::setAssignedAttendee);
        } else if (updated.getAssignedAttendee() == null) {
            task.setAssignedAttendee(null);
        }
        return taskRepository.save(task);
    }

    // Method for controller compatibility
    public Task updateTask(String eventId, String taskId, Task updated) {
        try {
            UUID uuid = UUID.fromString(taskId);
            return updateTask(uuid, updated);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid task ID format");
        }
    }

    // Method for controller compatibility with Long
    public Task updateTask(Long eventId, Long taskId, Task updated) {
        return updateTask(eventId.toString(), taskId.toString(), updated);
    }
    
    // Fix tasks with null titles/descriptions
    public int fixNullTaskData(String eventId) {
        try {
            UUID uuid = UUID.fromString(eventId);
            List<Task> tasks = getTasksByEvent(uuid);
            int fixedCount = 0;
            
            for (Task task : tasks) {
                boolean needsUpdate = false;
                
                if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
                    task.setTitle("Untitled Task");
                    needsUpdate = true;
                }
                
                if (task.getDescription() == null) {
                    task.setDescription("");
                    needsUpdate = true;
                }
                
                if (needsUpdate) {
                    taskRepository.save(task);
                    fixedCount++;
                }
            }
            
            return fixedCount;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid event ID format");
        }
    }
}
