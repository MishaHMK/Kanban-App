package com.kanban.project.dto.task;

public record MoveTaskDto(
        Long targetColumnId,
        Integer nextTaskPosition) {
}
