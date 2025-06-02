package com.tpt.chat_task.modules.workspace.entity;

import com.tpt.chat_task.modules.user.entity.User;
import com.tpt.chat_task.modules.workspace.enums.WORKSPACE_USER_ROLE;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "workspace_users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkspaceUser {
    @EmbeddedId
    private WorkspaceUserId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("workspaceId")
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 15)
    @NotNull(message = "Workspace user role cannot be null")
    private WORKSPACE_USER_ROLE userRole = WORKSPACE_USER_ROLE.MEMBER;

    @Column(nullable = false, name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;
}
