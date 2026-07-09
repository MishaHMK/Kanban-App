package com.kanban.project.dto.task;

import jakarta.validation.constraints.NotNull;

public record AssignTaskDto(
        @NotNull Long assigneeId
) {}