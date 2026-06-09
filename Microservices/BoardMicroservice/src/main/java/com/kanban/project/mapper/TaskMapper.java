package com.kanban.project.mapper;

import com.kanban.project.dto.task.TaskDto;
import com.kanban.project.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface TaskMapper {
    @Mapping(target = "columnId", source = "column.id")
    TaskDto toDto(Task task);
}
