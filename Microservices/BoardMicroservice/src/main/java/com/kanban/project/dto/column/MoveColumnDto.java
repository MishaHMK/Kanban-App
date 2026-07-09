package com.kanban.project.dto.column;

import jakarta.validation.constraints.NotNull;

public record MoveColumnDto(
        @NotNull Integer targetPosition
) {}