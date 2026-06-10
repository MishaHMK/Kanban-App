package com.kanban.project.mapper;

import com.kanban.project.dto.column.KanbanColumnDto;
import com.kanban.project.entity.KanbanColumn;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = TaskMapper.class)
public interface ColumnMapper {
    @Mapping(target = "boardId", source = "board.id")
    KanbanColumnDto toDto(KanbanColumn column);
}
