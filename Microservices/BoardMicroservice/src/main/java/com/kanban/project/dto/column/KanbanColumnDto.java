package com.kanban.project.dto.column;

import com.kanban.project.dto.task.TaskDto;

import java.util.List;

public record KanbanColumnDto(
        Long id,
        String name,
        Integer position,
        Long boardId,
        List<TaskDto> tasks
) {}