package com.tpt.chat_task.modules.task.service;

import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.task.dto.request.CreateTaskBoardRequest;
import com.tpt.chat_task.modules.task.dto.response.TaskBoardDetailResponse;
import com.tpt.chat_task.modules.task.dto.request.UpdateTaskBoardRequest;
import com.tpt.chat_task.modules.task.dto.response.TaskBoardResponse;
import com.tpt.chat_task.modules.task.dto.response.TaskGroupDetailResponse;
import com.tpt.chat_task.modules.task.entity.TaskBoard;
import com.tpt.chat_task.modules.workspace.dto.response.WorkspaceMemberResponse;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface TaskBoardService {
    public TaskBoardDetailResponse createTaskBoard(String token, String workspaceId, CreateTaskBoardRequest request) throws NotFoundException;
    public TaskBoardDetailResponse getTaskBoardDetail(String token, String workspaceId, String taskBoardId) throws NotFoundException, BadRequestException;
    public TaskBoardDetailResponse updateTaskBoard(String token, String workspaceId, String taskBoardId, UpdateTaskBoardRequest request) throws NotFoundException;
    public String deleteTaskBoard(String token, String workspaceId, String taskBoardId) throws NotFoundException;
    public List<TaskBoardDetailResponse> getListTaskBoards(String workspaceId) throws NotFoundException;
    public String togglePinTaskBoard(String workspaceId, String taskBoardId) throws NotFoundException;
    public TaskBoardDetailResponse addMemberToTaskBoard(String token, String workspaceId, String taskBoardId, String userId) throws NotFoundException, BadRequestException;
    public String deleteMemberFromTaskBoard(String token, String workspaceId, String taskBoardId, String userId) throws NotFoundException, BadRequestException;
    public List<WorkspaceMemberResponse> getListMembers(String workspaceId, String taskBoardId) throws NotFoundException;
    public List<TaskBoardResponse> getListTaskBoardsByWorkspaceAndUser(String workspaceId, String token) throws NotFoundException;
    public boolean isMemberTaskBoard(TaskBoard taskBoard, String userId) throws NotFoundException;
}
