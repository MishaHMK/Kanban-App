package com.kanban.project.mapper;

import com.kanban.project.dto.board.BoardCreateRequestDto;
import com.kanban.project.dto.board.BoardDto;
import com.kanban.project.dto.board.UpdateBoardDto;
import com.kanban.project.dto.column.KanbanColumnDto;
import com.kanban.project.entity.Board;
import com.kanban.project.entity.KanbanColumn;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = ColumnMapper.class)
public interface BoardMapper {
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "columns", ignore = true)
    Board toEntity(BoardCreateRequestDto boardCreateRequestDto);

    @Mapping(target = "columns", source = "columns")
    BoardDto toDto(Board board);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "columns", ignore = true)
    @Mapping(target = "collaboratorIds", ignore = true)
    void updateUserFromRequestDto(
            UpdateBoardDto updateDto, @MappingTarget Board board);
}
