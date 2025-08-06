package com.tpt.chat_task.modules.workspace.repository;

import com.tpt.chat_task.modules.workspace.entity.Workspace;
import com.tpt.chat_task.modules.workspace.entity.WorkspaceUser;
import com.tpt.chat_task.modules.workspace.entity.WorkspaceUserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WorkspaceUserRepository extends JpaRepository<WorkspaceUser, WorkspaceUserId> {
    @Query("SELECT wu.workspace FROM WorkspaceUser wu WHERE wu.user.id = :userId")
    Page<Workspace> findWorkspacesByUserId(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT wu.workspace FROM WorkspaceUser wu WHERE wu.workspace.id = :workspaceId AND wu.user.id = :userId")
    Optional<Workspace> findWorkspaceByWorkspaceIdAndUserId(@Param("userId") String userId, @Param("workspaceId") String workspaceId);

    @Query(value = """
        SELECT *
        FROM workspace_users
        WHERE workspace_id = :workspaceId AND user_id = :userId
    """, nativeQuery = true)
    Optional<WorkspaceUser> findWorkspaceUserByWorkspaceIdAndUserId(@Param("workspaceId") String workspaceId, @Param("userId") String userId);
}
