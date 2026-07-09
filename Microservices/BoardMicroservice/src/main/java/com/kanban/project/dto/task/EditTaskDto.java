package com.kanban.project.dto.task;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record EditTaskDto(
        @NotBlank String title,
        String description,
        @NotBlank String priority,
        LocalDateTime deadlineAt ) {
}
