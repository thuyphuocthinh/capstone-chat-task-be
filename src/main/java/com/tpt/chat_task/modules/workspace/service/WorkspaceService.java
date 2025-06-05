package com.tpt.chat_task.modules.workspace.service;

import com.tpt.chat_task.common.dto.SuccessResponseWithMetadata;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.workspace.dto.request.AddMemberRequest;
import com.tpt.chat_task.modules.workspace.dto.request.ChangeRoleRequest;
import com.tpt.chat_task.modules.workspace.dto.request.CreateWorkspaceRequest;
import com.tpt.chat_task.modules.workspace.dto.request.UpdateWorkspaceRequest;
import com.tpt.chat_task.modules.workspace.dto.response.WorkspaceResponse;
import org.apache.coyote.BadRequestException;

public interface WorkspaceService {
    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request);
    public SuccessResponseWithMetadata<?> getListWorkspaces(String token, Integer page, Integer paging) throws NotFoundException;
    public SuccessResponseWithMetadata<?> searchListWorkspaces(String token, String name, Integer page, Integer paging) throws NotFoundException;
    public WorkspaceResponse getWorkspaceById(String token, String id) throws NotFoundException;
    public WorkspaceResponse updateWorkspace(String id, UpdateWorkspaceRequest request) throws NotFoundException;
    public String addMemberToWorkspace(String workspaceId, AddMemberRequest request) throws NotFoundException;
    public String removeMemberFromWorkspace(String workspaceId, String userId) throws NotFoundException;
    public String changeRoleMemberFromWorkspace(String workspaceId, String userId, ChangeRoleRequest request) throws NotFoundException, BadRequestException;
}
