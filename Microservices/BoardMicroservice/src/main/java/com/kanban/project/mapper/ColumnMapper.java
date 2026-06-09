package com.kanban.project.mapper;

import com.kanban.project.dto.column.KanbanColumnDto;
import com.kanban.project.dto.task.TaskDto;
import com.kanban.project.entity.KanbanColumn;
import com.kanban.project.entity.Task;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = TaskMapper.class)
public interface ColumnMapper {
    @Mapping(target = "boardId", source = "board.id")
    KanbanColumnDto toDto(KanbanColumn column);
}
