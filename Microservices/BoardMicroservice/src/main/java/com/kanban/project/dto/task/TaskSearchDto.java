package com.kanban.project.dto.task;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TaskSearchDto(
        @NotNull Long boardId,
        String title,
        String priority,
        Long assigneeId,
        Long reporterId,
        LocalDateTime createdFrom,
        LocalDateTime createdTo,
        LocalDateTime deadlineFrom,
        LocalDateTime deadlineTo
) {}