package com.kanban.project.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateTaskDto(
        @NotBlank String title,
        String description,
        @NotBlank String priority,
        @NotNull Integer position,
        @NotNull Long columnId,
        LocalDateTime deadlineAt,
        Long assigneeId ) {
}
