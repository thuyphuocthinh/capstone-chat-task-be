package com.tpt.chat_task.modules.task.repository;

import com.tpt.chat_task.modules.task.entity.TaskBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskBoardRepository extends JpaRepository<TaskBoard, String> {
    @Query(value = """
        SELECT *
        FROM task_boards
        WHERE workspace_id = :workspaceId
    """, nativeQuery = true)
    List<TaskBoard> findAllByWorkspaceId(@Param("workspaceId") String workspaceId);

    @Query(value = """
        SELECT *
        FROM task_boards tb
        JOIN task_board_users tbu
        ON tb.id = tbu.task_board_id
        WHERE tbu.user_id = :userId AND tb.workspace_id = :workspaceId
    """, nativeQuery = true)
    List<TaskBoard> findAllByWorkspaceIdAndUserId(@Param("workspaceId") String workspaceId, @Param("userId") String userId);
}
