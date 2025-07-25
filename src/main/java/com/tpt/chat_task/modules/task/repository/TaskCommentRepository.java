package com.tpt.chat_task.modules.task.repository;

import com.tpt.chat_task.modules.task.entity.TaskComment;
import com.tpt.chat_task.modules.workspace.entity.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, String> {
    @Query("""
        SELECT t FROM TaskComment t
        WHERE t.task.id = :taskId
        ORDER BY t.createdAt DESC
    """)
    Page<TaskComment> findAllCommentsByTask(
            @Param("taskId") String taskId,
            Pageable pageable
    );

    @Query("""
        SELECT t FROM TaskComment t
        WHERE t.task.id = :taskId AND t.parentId = :parentTaskId
        ORDER BY t.createdAt DESC
    """)
    Page<TaskComment> findAllReplyCommentsByTaskAndCommentParent(
            @Param("taskId") String taskId,
            @Param("taskCommentParentId") String taskCommentParentId,
            Pageable pageable
    );

    @Query("""
        SELECT t FROM TaskComment t
        WHERE t.task.taskGroup.taskBoard.workspace.id = :workspaceId
        ORDER BY t.createdAt DESC
    """)
    Page<TaskComment> findAllTaskCommentsByWorkspace(
            @Param("workspaceId") String workspaceId,
            Pageable pageable
    );
}
