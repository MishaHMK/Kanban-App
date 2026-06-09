package com.kanban.project.service;

import com.kanban.project.config.BoardAppConfig;
import com.kanban.project.dto.board.BoardDto;
import com.kanban.project.dto.column.ColumnCreateDto;
import com.kanban.project.dto.column.KanbanColumnDto;
import com.kanban.project.dto.column.UpdateColumnNameDto;
import com.kanban.project.entity.Board;
import com.kanban.project.entity.KanbanColumn;
import com.kanban.project.error.BoardServiceException;
import com.kanban.project.error.model.ExceptionMessage;
import com.kanban.project.helper.BoardHelper;
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

    @Transactional
    public BoardDto addColumn(ColumnCreateDto request, Long requesterId) {
        Board board = boardRepository.findById(request.boardId())
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));

        boardHelper.verifyAccess(board, requesterId, false);

        if (columnRepository.countByBoardId(board.getId()) >= boardAppConfig.getColumnsLimit()) {
            throw new BoardServiceException(ExceptionMessage.BOARD_COLUMN_LIMIT_REACHED);
        }

        columnRepository.shiftRight(board.getId(), request.position());

        KanbanColumn newColumn = new KanbanColumn()
                .setName(request.name())
                .setPosition(request.position())
                .setBoard(board);

        columnRepository.saveAndFlush(newColumn);
        return boardService.buildBoardDto(request.boardId());
    }

    @Transactional
    public KanbanColumnDto updateColumnName(UpdateColumnNameDto request,
                                            Long columnId, Long requesterId) {
        KanbanColumn column = columnRepository.findById(columnId)
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));
        boardHelper.verifyAccess(column.getBoard(), requesterId, false);
        column.setName(request.newName());
        return columnMapper.toDto(columnRepository.save(column));
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

        return boardService.buildBoardDto(board.getId());
    }
}