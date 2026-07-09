package com.kanban.project.repository;

import com.kanban.project.entity.KanbanColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ColumnRepository extends JpaRepository<KanbanColumn, Long> {
    @Modifying
    @Query(value = """
        UPDATE kanban_columns
        SET position = position + 1
        WHERE board_id = :boardId AND position >= :position
        ORDER BY position DESC
    """, nativeQuery = true)
    void shiftRight(@Param("boardId") Long boardId, @Param("position") Integer position);

    @Modifying
    @Query(value = """
        UPDATE kanban_columns
        SET position = position - 1
        WHERE board_id = :boardId AND position > :position
        ORDER BY position ASC
    """, nativeQuery = true)
    void shiftLeft(@Param("boardId") Long boardId, @Param("position") Integer position);

    @Query("""
        SELECT c FROM KanbanColumn c
        LEFT JOIN FETCH c.tasks t
        WHERE c.board.ownerId = :userId
           OR :userId MEMBER OF c.board.collaboratorIds
        ORDER BY c.position, t.position
    """)
    List<KanbanColumn> findAllColumns(@Param("userId") Long userId);

    @Query("""
        SELECT c FROM KanbanColumn c
        LEFT JOIN FETCH c.tasks t
        WHERE c.board.id = :boardId
        ORDER BY c.position, t.position
    """)
    List<KanbanColumn> findColumnsWithTasks(@Param("boardId") Long boardId);

    @Modifying
    @Query(value = """
        UPDATE kanban_columns
        SET position = position - 1
        WHERE board_id = :boardId AND position > :oldPos AND position <= :newPos
        ORDER BY position ASC
    """, nativeQuery = true)
    void shiftRangeLeft(@Param("boardId") Long boardId,
                        @Param("oldPos")  Integer oldPos,
                        @Param("newPos")  Integer newPos);

    @Modifying
    @Query(value = """
        UPDATE kanban_columns
        SET position = position + 1
        WHERE board_id = :boardId AND position >= :newPos AND position < :oldPos
        ORDER BY position DESC
    """, nativeQuery = true)
    void shiftRangeRight(@Param("boardId") Long boardId,
                         @Param("newPos")  Integer newPos,
                         @Param("oldPos")  Integer oldPos);

    Integer countByBoardId(Long boardId);
}
