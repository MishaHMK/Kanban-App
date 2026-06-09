package com.kanban.project.controller;

import com.kanban.project.dto.board.BoardDto;
import com.kanban.project.dto.column.ColumnCreateDto;
import com.kanban.project.dto.column.KanbanColumnDto;
import com.kanban.project.dto.column.UpdateColumnNameDto;
import com.kanban.project.service.ColumnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/column")
@RequiredArgsConstructor
public class ColumnController {
    private final ColumnService columnService;

    @PostMapping("/create")
    public ResponseEntity<BoardDto> addColumnToBoard(
            @RequestBody ColumnCreateDto request,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(columnService.addColumn(request, userId));
    }

    @PatchMapping("/{columnId}")
    public ResponseEntity<KanbanColumnDto> renameColumn(
            @PathVariable Long columnId,
            @RequestBody UpdateColumnNameDto request,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(columnService.updateColumnName(request, columnId, userId));
    }

    @DeleteMapping("/{columnId}")
    public ResponseEntity<BoardDto> deleteColumnFromBoard(
            @PathVariable Long columnId,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(columnService.deleteColumn(columnId, userId));
    }
}
