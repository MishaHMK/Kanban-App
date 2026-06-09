package com.kanban.project.service;

import com.kanban.project.dto.board.BoardCollaboratorActionDto;
import com.kanban.project.dto.board.BoardCreateRequestDto;
import com.kanban.project.dto.board.BoardDto;
import com.kanban.project.dto.board.UpdateBoardDto;
import com.kanban.project.entity.Board;
import com.kanban.project.entity.KanbanColumn;
import com.kanban.project.entity.Task;
import com.kanban.project.error.BoardServiceException;
import com.kanban.project.error.model.ExceptionMessage;
import com.kanban.project.helper.BoardHelper;
import com.kanban.project.helper.BoardSyncHelper;
import com.kanban.project.mapper.BoardMapper;
import com.kanban.project.repository.BoardRepository;
import com.kanban.project.repository.ColumnRepository;
import com.kanban.project.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final ColumnRepository columnRepository;
    private final TaskRepository taskRepository;
    private final BoardMapper boardMapper;
    private final BoardHelper boardHelper;
    private final BoardSyncHelper boardSyncHelper;

    @Transactional
    public BoardDto createBoard(BoardCreateRequestDto request, Long userId) {
        Board newBoard = boardMapper.toEntity(request);
        newBoard.setOwnerId(userId);
        boardRepository.save(newBoard);
        return buildBoardDto(newBoard.getId());
    }

    public BoardDto findBoardById(Long boardId, Long userId) {
        boardRepository.findUserBoardById(boardId, userId)
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));
        return buildBoardDto(boardId);
    }

    public List<BoardDto> getAllBoards(Long userId) {
        List<Board> boards = boardRepository.findUserBoards(userId);
        List<KanbanColumn> columns = columnRepository.findAllColumns(userId);
        List<Task> tasks = taskRepository.findUserTasks(userId);

        Map<Long, List<Task>> tasksByColumn =
                tasks.stream().collect(Collectors.groupingBy(task ->
                        task.getColumn().getId()));
        Map<Long, List<KanbanColumn>> columnsByBoard =
                columns.stream().collect(Collectors.groupingBy(column ->
                        column.getBoard().getId()));

        columns.forEach(column -> column.setTasks(tasksByColumn.
                getOrDefault(column.getId(), List.of())));
        boards.forEach(board -> board.setColumns(columnsByBoard.
                getOrDefault(board.getId(), List.of())));

        return boards.stream().map(boardMapper::toDto).toList();
    }

    @Transactional
    public void deleteBoardById(Long boardId, Long userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));
        boardHelper.verifyAccess(board, userId, true);
        boardRepository.delete(board);
    }

    @Transactional
    public BoardDto updateBoard(Long boardId, Long userId, UpdateBoardDto request) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));
        boardHelper.verifyAccess(board, userId, false);
        boardMapper.updateUserFromRequestDto(request, board);
        boardRepository.save(board);
        BoardDto result = buildBoardDto(boardId);
        boardSyncHelper.shareFullBoard(boardId);
        return result;
    }

    @Transactional
    public BoardDto collaboratorOperation(Long boardId, Long ownerId,
                                          BoardCollaboratorActionDto actionDto) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));
        boardHelper.verifyAccess(board, ownerId, true);

        switch (actionDto.action()) {
            case ADD -> board.getCollaboratorIds().add(actionDto.collaboratorId());
            case REMOVE -> board.getCollaboratorIds().remove(actionDto.collaboratorId());
        }
        boardRepository.save(board);
        BoardDto result = buildBoardDto(boardId);
        boardSyncHelper.shareFullBoard(boardId);
        return result;
    }

    public BoardDto buildBoardDto(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));
        List<KanbanColumn> columns = columnRepository.findColumnsWithTasks(boardId);
        board.setColumns(columns);
        return boardMapper.toDto(board);
    }
}