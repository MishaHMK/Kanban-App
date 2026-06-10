package com.kanban.project.helper;

import com.kanban.project.client.NotificationClient;
import com.kanban.project.entity.Board;
import com.kanban.project.entity.KanbanColumn;
import com.kanban.project.error.BoardServiceException;
import com.kanban.project.error.model.ExceptionMessage;
import com.kanban.project.mapper.BoardMapper;
import com.kanban.project.repository.BoardRepository;
import com.kanban.project.repository.ColumnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BoardSyncHelper {
    private final BoardRepository boardRepository;
    private final ColumnRepository columnRepository;
    private final NotificationClient notificationClient;
    private final BoardMapper boardMapper;

    @Transactional(readOnly = true)
    public void shareFullBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));

        List<KanbanColumn> columns = columnRepository.findColumnsWithTasks(boardId);
        board.setColumns(columns);

        notificationClient.shareFullBoardUpdate(boardId, boardMapper.toDto(board));
    }
}