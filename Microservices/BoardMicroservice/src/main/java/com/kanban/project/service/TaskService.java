package com.kanban.project.service;

import com.kanban.project.config.BoardAppConfig;
import com.kanban.project.data.Priority;
import com.kanban.project.dto.task.*;
import com.kanban.project.entity.Board;
import com.kanban.project.entity.KanbanColumn;
import com.kanban.project.entity.Task;
import com.kanban.project.error.BoardServiceException;
import com.kanban.project.error.model.ExceptionMessage;
import com.kanban.project.helper.BoardHelper;
import com.kanban.project.helper.BoardSyncHelper;
import com.kanban.project.helper.TaskSpecification;
import com.kanban.project.mapper.TaskMapper;
import com.kanban.project.repository.BoardRepository;
import com.kanban.project.repository.ColumnRepository;
import com.kanban.project.repository.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ColumnRepository columnRepository;
    private final BoardRepository boardRepository;
    private final BoardHelper boardHelper;
    private final TaskMapper taskMapper;
    private final BoardSyncHelper boardSyncHelper;
    private final BoardAppConfig boardAppConfig;

    @Transactional
    public TaskDto createTask(CreateTaskDto request, Long requesterId) {
        KanbanColumn column = columnRepository.findById(request.columnId())
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));

        Board board = column.getBoard();
        boardHelper.verifyAccess(board, requesterId, false);

        if (taskRepository.countByColumnId(column.getId()) >= boardAppConfig.getTasksLimit()) {
            throw new BoardServiceException(ExceptionMessage.COLUMN_TASK_LIMIT_REACHED);
        }

        if (request.assigneeId() != null) {
            boolean isOwner = board.getOwnerId().equals(request.assigneeId());
            boolean isCollaborator = board.getCollaboratorIds().contains(request.assigneeId());
            if (!isOwner && !isCollaborator) {
                throw new BoardServiceException(ExceptionMessage.FORBIDDEN);
            }
        }

        int safePosition = countSafeCreatePosition(column, request.position());
        taskRepository.shiftRight(column.getId(), safePosition);
        taskRepository.flush();

        Task task = new Task()
                .setTitle(request.title())
                .setDescription(request.description())
                .setPriority(Priority.get(request.priority()))
                .setPosition(safePosition)
                .setReporterId(requesterId)
                .setAssigneeId(request.assigneeId())
                .setDeadlineAt(request.deadlineAt())
                .setColumn(column);

        TaskDto newTask = taskMapper.toDto(taskRepository.save(task));
        boardSyncHelper.shareFullBoard(task.getColumn().getBoard().getId());
        return newTask;
    }

    @Transactional
    public TaskDto editTask(Long taskId, EditTaskDto request, Long requesterId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));

        boardHelper.verifyAccess(task.getColumn().getBoard(), requesterId, false);

        task.setTitle(request.title())
                .setDescription(request.description())
                .setPriority(Priority.get(request.priority()))
                .setDeadlineAt(request.deadlineAt());

        TaskDto result = taskMapper.toDto(taskRepository.save(task));
        boardSyncHelper.shareFullBoard(task.getColumn().getBoard().getId());
        return result;
    }

    @Transactional
    public void moveTask(Long taskId, MoveTaskDto request, Long requesterId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));

        boardHelper.verifyAccess(task.getColumn().getBoard(), requesterId, false);

        Long boardId  = task.getColumn().getBoard().getId();
        Long oldColId = task.getColumn().getId();
        Long newColId = request.targetColumnId();
        Integer oldPos = task.getPosition();

        long targetCount = taskRepository.countByColumnId(newColId);

        int maxPos = oldColId.equals(newColId)
                ? (int) targetCount
                : (int) targetCount + 1;

        int newPos = Math.max(1, Math.min(request.nextTaskPosition(), maxPos));

        if (oldColId.equals(newColId) && oldPos.equals(newPos)) return;

        task.setPosition(-task.getId().intValue());
        taskRepository.saveAndFlush(task);

        if (oldColId.equals(newColId)) {
            if (newPos > oldPos) {
                taskRepository.shiftRangeLeft(oldColId, oldPos, newPos);
            } else {
                taskRepository.shiftRangeRight(oldColId, newPos, oldPos);
            }
        } else {
            if (targetCount >= boardAppConfig.getTasksLimit()) {
                throw new BoardServiceException(ExceptionMessage.COLUMN_TASK_LIMIT_REACHED);
            }
            taskRepository.shiftLeft(oldColId, oldPos);
            taskRepository.shiftRight(newColId, newPos);
        }

        KanbanColumn targetColumn = columnRepository.findById(newColId)
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));

        task.setColumn(targetColumn);
        task.setPosition(newPos);
        taskRepository.save(task);

        boardSyncHelper.shareFullBoard(boardId);
    }

    @Transactional
    public void deleteTask(Long taskId, Long requesterId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));

        boardHelper.verifyAccess(task.getColumn().getBoard(), requesterId, false);

        Long boardId = task.getColumn().getBoard().getId();
        taskRepository.delete(task);
        boardSyncHelper.shareFullBoard(boardId);
    }

    @Transactional
    public TaskDto assignTask(Long taskId, AssignTaskDto request, Long requesterId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));

        Board board = task.getColumn().getBoard();
        boardHelper.verifyAccess(board, requesterId, false);

        boolean isOwner = board.getOwnerId().equals(request.assigneeId());
        boolean isCollaborator = board.getCollaboratorIds().contains(request.assigneeId());

        if (!isOwner && !isCollaborator) {
            throw new BoardServiceException(ExceptionMessage.FORBIDDEN);
        }

        task.setAssigneeId(request.assigneeId());
        TaskDto result = taskMapper.toDto(taskRepository.save(task));
        boardSyncHelper.shareFullBoard(board.getId());
        return result;
    }

    @Transactional
    public TaskDto unassignTask(Long taskId, Long requesterId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));

        boardHelper.verifyAccess(task.getColumn().getBoard(), requesterId, false);

        task.setAssigneeId(null);
        TaskDto result = taskMapper.toDto(taskRepository.save(task));
        boardSyncHelper.shareFullBoard(task.getColumn().getBoard().getId());
        return result;
    }

    Integer countTasksInColumn(KanbanColumn column) {
        return taskRepository.countByColumnId(column.getId());
    }

    private int countSafeCreatePosition(KanbanColumn column, int position) {
        int taskCount = countTasksInColumn(column);
        return Math.max(1, Math.min(position, taskCount + 1));
    }

    @Transactional()
    public List<TaskDto> searchTasks(TaskSearchDto request, Long requesterId) {
        Board board = boardRepository.findById(request.boardId())
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));

        boardHelper.verifyAccess(board, requesterId, false);

        Specification<Task> spec = Specification
                .where(TaskSpecification.belongsToBoard(request.boardId()));

        if (request.title() != null && !request.title().isBlank()) {
            spec = spec.and(TaskSpecification.titleContains(request.title()));
        }

        if (request.priority() != null && !request.priority().isBlank()) {
            spec = spec.and(TaskSpecification.hasPriority(request.priority()));
        }

        if (request.assigneeId() != null) {
            spec = spec.and(TaskSpecification.hasAssignee(request.assigneeId()));
        }
        if (request.reporterId() != null) {
            spec = spec.and(TaskSpecification.hasReporter(request.reporterId()));
        }
        if (request.createdFrom() != null) {
            spec = spec.and(TaskSpecification.createdAfter(request.createdFrom()));
        }
        if (request.createdTo() != null) {
            spec = spec.and(TaskSpecification.createdBefore(request.createdTo()));
        }
        if (request.deadlineFrom() != null) {
            spec = spec.and(TaskSpecification.deadlineAfter(request.deadlineFrom()));
        }
        if (request.deadlineTo() != null) {
            spec = spec.and(TaskSpecification.deadlineBefore(request.deadlineTo()));
        }

        return taskRepository.findAll(spec)
                .stream()
                .map(taskMapper::toDto)
                .toList();
    }
}
