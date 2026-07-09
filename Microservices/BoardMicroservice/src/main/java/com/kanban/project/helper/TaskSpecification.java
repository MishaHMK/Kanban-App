package com.kanban.project.helper;

import com.kanban.project.data.Priority;
import com.kanban.project.entity.Task;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class TaskSpecification {
    public static Specification<Task> belongsToBoard(Long boardId) {
        return (root, query, cb) ->
                cb.equal(root.get("column").get("board").get("id"), boardId);
    }

    public static Specification<Task> titleContains(String title) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Task> hasPriority(String priority) {
        return (root, query, cb) ->
                cb.equal(root.get("priority"), Priority.get(priority));
    }

    public static Specification<Task> hasAssignee(Long assigneeId) {
        return (root, query, cb) ->
                cb.equal(root.get("assigneeId"), assigneeId);
    }

    public static Specification<Task> hasReporter(Long reporterId) {
        return (root, query, cb) ->
                cb.equal(root.get("reporterId"), reporterId);
    }

    public static Specification<Task> createdAfter(LocalDateTime from) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<Task> createdBefore(LocalDateTime to) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    public static Specification<Task> deadlineAfter(LocalDateTime from) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("deadlineAt"), from);
    }

    public static Specification<Task> deadlineBefore(LocalDateTime to) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("deadlineAt"), to);
    }
}