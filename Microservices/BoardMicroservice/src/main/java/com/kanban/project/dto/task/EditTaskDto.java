package com.kanban.project.dto.task;

public record EditTaskDto(
        String title,
        String description,
        String priority) {
}
