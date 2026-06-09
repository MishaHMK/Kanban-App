package com.kanban.project.controller;

import com.kanban.project.dto.board.BoardCollaboratorActionDto;
import com.kanban.project.dto.board.BoardCreateRequestDto;
import com.kanban.project.dto.board.BoardDto;
import com.kanban.project.dto.board.UpdateBoardDto;
import com.kanban.project.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @PostMapping("/create")
    public ResponseEntity<BoardDto> createBoard(
            @Valid @RequestBody BoardCreateRequestDto request,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(boardService.createBoard(request, userId));
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDto> findBoard(
            @PathVariable Long boardId,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(boardService.findBoardById(boardId, userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<BoardDto>> findAllBoards(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(boardService.getAllBoards(userId));
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(
            @PathVariable Long boardId,
            @RequestHeader("X-User-Id") Long userId) {
        boardService.deleteBoardById(boardId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{boardId}")
    public ResponseEntity<BoardDto> updateBoardName(
            @Valid @RequestBody UpdateBoardDto request,
            @PathVariable Long boardId,
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(boardService.updateBoard(boardId, userId, request)); // called once
    }

    @PatchMapping("/{boardId}/collaborator")
    public ResponseEntity<BoardDto> takeCollaboratorAction(
            @RequestBody BoardCollaboratorActionDto request,
            @PathVariable Long boardId,
            @RequestHeader("X-User-Id") Long ownerId) {
        return ResponseEntity.ok(boardService.collaboratorOperation(boardId, ownerId, request));
    }
}
