package com.kanban.project.dto.board;

import com.kanban.project.dto.column.KanbanColumnDto;

import java.util.List;
import java.util.Set;

public record BoardDto(
        Long id,
        String name,
        Long ownerId,
        List<KanbanColumnDto> columns,
        Set<Long> collaboratorIds
) {}
