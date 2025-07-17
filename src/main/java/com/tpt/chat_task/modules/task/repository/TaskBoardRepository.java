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
}
