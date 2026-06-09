package com.kanban.project.repository;

import com.kanban.project.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {
    @Query("""
        SELECT b FROM Board b
        WHERE b.id = :boardId
          AND (b.ownerId = :userId OR :userId MEMBER OF b.collaboratorIds)
    """)
    Optional<Board> findUserBoardById(@Param("boardId") Long boardId,
                                      @Param("userId")  Long userId);

    @Query("""
        SELECT DISTINCT b FROM Board b
        WHERE b.ownerId = :userId
           OR :userId MEMBER OF b.collaboratorIds
    """)
    List<Board> findUserBoards(@Param("userId") Long userId);
}
