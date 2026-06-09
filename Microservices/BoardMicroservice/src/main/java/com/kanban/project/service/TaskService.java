package com.kanban.project.service;

import com.kanban.project.config.BoardAppConfig;
import com.kanban.project.data.Priority;
import com.kanban.project.dto.task.CreateTaskDto;
import com.kanban.project.dto.task.EditTaskDto;
import com.kanban.project.dto.task.MoveTaskDto;
import com.kanban.project.dto.task.TaskDto;
import com.kanban.project.entity.KanbanColumn;
import com.kanban.project.entity.Task;
import com.kanban.project.error.BoardServiceException;
import com.kanban.project.error.model.ExceptionMessage;
import com.kanban.project.helper.BoardHelper;
import com.kanban.project.helper.BoardSyncHelper;
import com.kanban.project.mapper.TaskMapper;
import com.kanban.project.repository.ColumnRepository;
import com.kanban.project.repository.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ColumnRepository columnRepository;
    private final BoardHelper boardHelper;
    private final TaskMapper taskMapper;
    private final BoardSyncHelper boardSyncHelper;
    private final BoardAppConfig boardAppConfig;

    @Transactional
    public TaskDto createTask(CreateTaskDto request, Long requesterId) {
        KanbanColumn column = columnRepository.findById(request.columnId())
                .orElseThrow(() -> new BoardServiceException(ExceptionMessage.NOT_FOUND));

        boardHelper.verifyAccess(column.getBoard(), requesterId, false);

        if (taskRepository.countByColumnId(column.getId()) >= boardAppConfig.getTasksLimit()) {
            throw new BoardServiceException(ExceptionMessage.COLUMN_TASK_LIMIT_REACHED);
        }

        taskRepository.shiftRight(column.getId(), request.position());
        taskRepository.flush();

        Task task = new Task()
                .setTitle(request.title())
                .setDescription(request.description())
                .setPriority(Priority.get(request.priority()))
                .setPosition(request.position())
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
                .setPriority(Priority.get(request.priority()));

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
        Integer newPos = request.nextTaskPosition();

        if (oldColId.equals(newColId) && oldPos.equals(newPos)) return;

        task.setPosition(-task.getId().intValue());
        taskRepository.saveAndFlush(task);

        if (oldColId.equals(newColId)) {
            if (newPos > oldPos) {
                taskRepository.shiftRangeLeft(oldColId, oldPos, newPos);
            }
            else {
                taskRepository.shiftRangeRight(oldColId, newPos, oldPos);
            }
        } else {
            if (taskRepository.countByColumnId(newColId) >= boardAppConfig.getTasksLimit()) {
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
}
