package com.tpt.chat_task.modules.task.service;

import com.tpt.chat_task.common.dto.SuccessResponseWithMetadata;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.task.dto.request.CreateTaskRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateTaskRequest;
import com.tpt.chat_task.modules.task.dto.response.TaskDetailResponse;
import com.tpt.chat_task.modules.task.dto.response.TaskResourceResponse;
import com.tpt.chat_task.modules.workspace.dto.response.WorkspaceMemberResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface TaskService {
    public TaskDetailResponse addNewTask(String token, String taskGroupId, CreateTaskRequest createTaskRequest) throws NotFoundException;
    public TaskDetailResponse updateTask(String taskGroupId, String taskId, UpdateTaskRequest updateTaskRequest) throws NotFoundException;
    public String deleteTask(String taskGroupId, String taskId) throws NotFoundException;
    // filter task
    public SuccessResponseWithMetadata<?> getListTasksByGroupId(String token, String taskGroupId, Integer page, Integer paging) throws NotFoundException;
    public String addMemberToTask(String taskId, String userId) throws NotFoundException, BadRequestException;
    public String removeMemberFromTask(String taskId, String userId) throws NotFoundException, BadRequestException;
    public List<WorkspaceMemberResponse> getMembersOfTask(String taskId) throws NotFoundException;
    public String changeTaskPositionInSameGroup(String taskGroupId, String taskId, int newPosition) throws NotFoundException, BadRequestException;
    public String changeTaskPositionInDiffGroup(
            String currentTaskGroupId,
            String newTaskGroupId,
            String taskId,
            int newPosition
    ) throws NotFoundException;
    public TaskDetailResponse toggleLabelTask(String taskId, String labelId) throws NotFoundException;
    public TaskDetailResponse addNewFiles(String taskId, List<MultipartFile> files) throws NotFoundException, IOException;
    public TaskDetailResponse getDetailedTask(String taskGroupId, String taskId) throws NotFoundException;
    public boolean isMemberOfTask(String taskId, String userID) throws NotFoundException, BadRequestException;
}
