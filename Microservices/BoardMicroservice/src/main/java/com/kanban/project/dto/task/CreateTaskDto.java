package com.kanban.project.dto.task;

public record CreateTaskDto(
        String title,
        String description,
        String priority,
        Integer position,
        Long columnId) {
}
