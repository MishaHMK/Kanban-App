package com.kanban.project.dto.task;

import java.time.LocalDateTime;

public record TaskDto(
    Long id,
    String title,
    String description,
    String priority,
    Integer position,
    Long columnId,
    Long reporterId,
    Long assigneeId,
    LocalDateTime createdAt,
    LocalDateTime deadlineAt
) { }