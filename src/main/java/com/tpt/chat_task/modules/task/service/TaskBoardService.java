package com.tpt.chat_task.modules.task.service;

import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.task.dto.request.CreateTaskBoardRequest;
import com.tpt.chat_task.modules.task.dto.response.TaskBoardDetailResponse;
import com.tpt.chat_task.modules.task.dto.request.UpdateTaskBoardRequest;

import java.util.List;

public interface TaskBoardService {
    public TaskBoardDetailResponse createTaskBoard(String workspaceId, CreateTaskBoardRequest request) throws NotFoundException;
    public TaskBoardDetailResponse getTaskBoardDetail(String workspaceId, String taskBoardId) throws NotFoundException;
    public TaskBoardDetailResponse updateTaskBoard(String workspaceId, String taskBoardId, UpdateTaskBoardRequest request) throws NotFoundException;
    public String deleteTaskBoard(String workspaceId, String taskBoardId) throws NotFoundException;
    public List<TaskBoardDetailResponse> getListTaskBoards(String workspaceId) throws NotFoundException;
    public String togglePinTaskBoard(String workspaceId, String taskBoardId) throws NotFoundException;
}
