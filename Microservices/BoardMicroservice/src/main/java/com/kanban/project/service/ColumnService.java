package com.kanban.project.service;

import com.kanban.project.config.BoardAppConfig;
import com.kanban.project.dto.board.BoardDto;
import com.kanban.project.dto.column.ColumnCreateDto;
import com.kanban.project.dto.column.KanbanColumnDto;
import com.kanban.project.dto.column.MoveColumnDto;
import com.kanban.project.dto.column.UpdateColumnNameDto;
import com.kanban.project.entity.Board;
import com.kanban.project.entity.KanbanColumn;
import com.kanban.project.error.BoardServiceException;
import com.kanban.project.error.model.ExceptionMessage;
import com.kanban.project.helper.BoardHelper;
import com.kanban.project.helper.BoardSyncHelper;
import com.kanban.project.mapper.ColumnMapper;
import com.kanban.project.repository.BoardRepository;
import com.kanban.project.repository.ColumnRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ColumnService {
    private final ColumnRepository columnRepository;
    private final BoardRepository  boardRepository;
    private final ColumnMapper columnMapper;
    private final BoardHelper boardHelper;
    private final BoardService boardService;
    private final BoardAppConfig boardAppConfig;
    private final BoardSyncHelper boardSyncHelper;

    @Transactional
    public BoardDto addColumn(ColumnCreateDto request, Long requesterId) {
        Board board = boardRepository.findById(request.boardId())
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));

        boardHelper.verifyAccess(board, requesterId, false);

        if (columnRepository.countByBoardId(board.getId()) >= boardAppConfig.getColumnsLimit()) {
            throw new BoardServiceException(ExceptionMessage.BOARD_COLUMN_LIMIT_REACHED);
        }

        int safePosition = countSafeCreatePosition(board, request.position());
        columnRepository.shiftRight(board.getId(), safePosition);

        KanbanColumn newColumn = new KanbanColumn()
                .setName(request.name())
                .setPosition(safePosition)
                .setBoard(board);

        columnRepository.saveAndFlush(newColumn);
        boardSyncHelper.shareFullBoard(newColumn.getBoard().getId());
        return boardService.buildBoardDto(request.boardId());
    }

    @Transactional
    public KanbanColumnDto updateColumnName(UpdateColumnNameDto request,
                                            Long columnId, Long requesterId) {
        KanbanColumn column = columnRepository.findById(columnId)
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));
        boardHelper.verifyAccess(column.getBoard(), requesterId, false);
        column.setName(request.newName());

        boardSyncHelper.shareFullBoard(column.getBoard().getId());
        return columnMapper.toDto(columnRepository.save(column));
    }

    @Transactional
    public BoardDto moveColumn(Long columnId, MoveColumnDto request, Long requesterId) {
        KanbanColumn column = columnRepository.findById(columnId)
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));

        Board board = column.getBoard();
        boardHelper.verifyAccess(board, requesterId, false);

        Integer oldPos = column.getPosition();
        int newPos = countSafeMovePosition(board, request.targetPosition());

        if (oldPos.equals(newPos)) return boardService.buildBoardDto(board.getId());

        column.setPosition(-columnId.intValue());
        columnRepository.saveAndFlush(column);

        if (newPos > oldPos) {
            columnRepository.shiftRangeLeft(board.getId(), oldPos, newPos);
        } else {
            columnRepository.shiftRangeRight(board.getId(), newPos, oldPos);
        }

        column.setPosition(newPos);
        columnRepository.save(column);

        boardSyncHelper.shareFullBoard(board.getId());
        return boardService.buildBoardDto(board.getId());
    }

    @Transactional
    public BoardDto deleteColumn(Long columnId, Long requesterId) {
        KanbanColumn column = columnRepository.findById(columnId)
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));

        Board board = column.getBoard();
        boardHelper.verifyAccess(board, requesterId, true);

        Integer deletedPosition = column.getPosition();
        columnRepository.delete(column);
        columnRepository.flush();
        columnRepository.shiftLeft(board.getId(), deletedPosition);

        boardSyncHelper.shareFullBoard(column.getBoard().getId());
        return boardService.buildBoardDto(board.getId());
    }

    private int countSafeCreatePosition(Board board, int position) {
        int columnCount = columnRepository.countByBoardId(board.getId());
        return Math.max(1, Math.min(position, columnCount + 1));
    }

    private int countSafeMovePosition(Board board, int position) {
        int columnCount = columnRepository.countByBoardId(board.getId());
        return Math.max(1, Math.min(position, columnCount));
    }
}