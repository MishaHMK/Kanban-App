package com.kanban.project.dto.task;


public record TaskDto(
    Long id,
    String title,
    String description,
    String priority,
    Integer position,
    Long columnId) {
}