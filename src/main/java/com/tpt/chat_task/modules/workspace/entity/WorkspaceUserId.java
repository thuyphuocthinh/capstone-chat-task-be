package com.tpt.chat_task.modules.workspace.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class WorkspaceUserId implements Serializable {
    private String userId;
    private String workspaceId;
}
