package com.tpt.chat_task.modules.workspace.repository;

import com.tpt.chat_task.modules.workspace.entity.Workspace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, String> {
    Page<Workspace> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("""
        SELECT w FROM Workspace w
        JOIN w.workspaceUsers wu
        WHERE wu.user.id = :userId
        AND LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<Workspace> searchByUserIdAndName(
            @Param("userId") String userId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
            SELECT w FROM Workspace w
            JOIN w.workspaceUsers wu
            WHERE wu.user.id = :userId AND wu.workspace.id = :workspaceId
        """)
    Optional<Workspace> findByName(@Param("userId") String userId, @Param("workspaceId") String workspaceId);
}
