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
        WHERE t.task.id = :taskId AND t.parentId IS NULL
        ORDER BY t.createdAt DESC
    """)
    Page<TaskComment> findAllCommentsByTask(
            @Param("taskId") String taskId,
            Pageable pageable
    );

    @Query("""
        SELECT t FROM TaskComment t
        WHERE t.task.id = :taskId AND t.parentId = :taskCommentParentId
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
    AND (t.parentId IS NOT NULL OR t.isThreadRoot = true)
    ORDER BY t.createdAt DESC
    """)
        Page<TaskComment> findAllTaskCommentsByWorkspace(
                @Param("workspaceId") String workspaceId,
                Pageable pageable
        );

    @Query("""
        SELECT t FROM TaskComment t
        WHERE t.task.taskGroup.taskBoard.workspace.id = :workspaceId 
        AND t.isThreadRoot = true
        ORDER BY t.createdAt DESC
    """)
    Page<TaskComment> findThreadRootsByWorkspace(@Param("workspaceId") String workspaceId, Pageable pageable);

    @Query("""
        SELECT t FROM TaskComment t
        WHERE t.parentId IN :parentIds
    """)
    List<TaskComment> findRepliesByParentIds(@Param("parentIds") List<String> parentIds);

}
