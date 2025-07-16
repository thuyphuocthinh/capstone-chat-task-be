package com.tpt.chat_task.modules.workspace.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class WorkspaceUserId implements Serializable {
    private String userId;
    private String workspaceId;

    public WorkspaceUserId(String userId, String workspaceId) {
        this.userId = userId;
        this.workspaceId = workspaceId;
    }

    public WorkspaceUserId() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkspaceUserId)) return false;
        WorkspaceUserId that = (WorkspaceUserId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(workspaceId, that.workspaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, workspaceId);
    }
}
