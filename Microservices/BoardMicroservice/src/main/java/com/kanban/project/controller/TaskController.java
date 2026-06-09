package com.kanban.project.controller;

import com.kanban.project.dto.task.CreateTaskDto;
import com.kanban.project.dto.task.EditTaskDto;
import com.kanban.project.dto.task.MoveTaskDto;
import com.kanban.project.dto.task.TaskDto;
import com.kanban.project.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/create")
    public ResponseEntity<TaskDto> createTask(
            @Valid @RequestBody CreateTaskDto request,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(taskService.createTask(request, userId));
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskDto> editTask(
            @PathVariable Long taskId,
            @Valid @RequestBody EditTaskDto request,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(taskService.editTask(taskId, request, userId));
    }

    @PatchMapping("/move/{taskId}")
    public ResponseEntity<Void> moveTask(
            @PathVariable Long taskId,
            @Valid @RequestBody MoveTaskDto request,
            @RequestHeader("X-User-Id") Long userId) {
        taskService.moveTask(taskId, request, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long taskId,
            @RequestHeader("X-User-Id") Long userId) {
        taskService.deleteTask(taskId, userId);
        return ResponseEntity.noContent().build();
    }
}