package com.example.demo.controller;

import com.example.demo.model.Task;
import com.example.demo.service.TaskService;
import com.example.demo.websocket.EventProgressWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events/{eventId}/tasks")
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    @Autowired
    private TaskService taskService;
    
    @Autowired
    private EventProgressWebSocketHandler webSocketHandler;

    @GetMapping
    public List<Task> getTasksForEvent(@PathVariable String eventId) {
        return taskService.getTasksForEvent(eventId);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@PathVariable String eventId, @RequestBody Task task) {
        Task createdTask = taskService.createTask(eventId, task);
        
        // Broadcast real-time creation
        webSocketHandler.broadcastTaskCreation(eventId, createdTask);
        
        return ResponseEntity.ok(createdTask);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(
            @PathVariable String eventId,
            @PathVariable String taskId, 
            @RequestBody Task taskUpdate) {
        
        Task updatedTask = taskService.updateTask(eventId, taskId, taskUpdate);
        
        // Broadcast real-time update
        webSocketHandler.broadcastTaskUpdate(eventId, updatedTask);
        
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable String eventId, @PathVariable String taskId) {
        taskService.deleteTask(eventId, taskId);
        
        // Broadcast deletion update - send the taskId string for frontend compatibility
        webSocketHandler.broadcastTaskDeletion(eventId, taskId);
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/fix-null-data")
    public ResponseEntity<String> fixNullTaskData(@PathVariable String eventId) {
        int fixedCount = taskService.fixNullTaskData(eventId);
        return ResponseEntity.ok("Fixed " + fixedCount + " tasks with null data");
    }
}
