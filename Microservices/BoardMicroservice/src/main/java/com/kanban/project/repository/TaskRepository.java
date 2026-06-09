package com.kanban.project.repository;

import com.kanban.project.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    long countByColumnId(Long columnId);

    @Query("""
        SELECT DISTINCT t FROM Task t
        WHERE t.column.board.ownerId = :userId
           OR :userId MEMBER OF t.column.board.collaboratorIds
        ORDER BY t.position
    """)
    List<Task> findUserTasks(@Param("userId") Long userId);

    @Modifying(flushAutomatically = true)
    @Query(value = """
        UPDATE tasks SET position = position + 1
        WHERE column_id = :columnId AND position >= :pos
        ORDER BY position DESC
    """, nativeQuery = true)
    void shiftRight(@Param("columnId") Long columnId, @Param("pos") Integer pos);

    @Modifying(flushAutomatically = true)
    @Query(value = """
        UPDATE tasks SET position = position - 1
        WHERE column_id = :columnId AND position > :pos
        ORDER BY position ASC
    """, nativeQuery = true)
    void shiftLeft(@Param("columnId") Long columnId, @Param("pos") Integer pos);

    @Modifying(flushAutomatically = true)
    @Query(value = """
        UPDATE tasks SET position = position - 1
        WHERE column_id = :columnId AND position > :oldPos AND position <= :newPos
        ORDER BY position ASC
    """, nativeQuery = true)
    void shiftRangeLeft(@Param("columnId") Long columnId,
                        @Param("oldPos")   Integer oldPos,
                        @Param("newPos")   Integer newPos);

    @Modifying(flushAutomatically = true)
    @Query(value = """
        UPDATE tasks SET position = position + 1
        WHERE column_id = :columnId AND position >= :newPos AND position < :oldPos
        ORDER BY position DESC
    """, nativeQuery = true)
    void shiftRangeRight(@Param("columnId") Long columnId,
                         @Param("newPos")   Integer newPos,
                         @Param("oldPos")   Integer oldPos);
}
