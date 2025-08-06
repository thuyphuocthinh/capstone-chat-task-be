package com.tpt.chat_task.modules.task.service;

import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.task.dto.request.CreateTaskGroupRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateTaskGroupRequest;
import com.tpt.chat_task.modules.task.dto.response.TaskGroupDetailResponse;
import org.apache.coyote.BadRequestException;

public interface TaskGroupService {
    public TaskGroupDetailResponse addNewTaskGroup(String taskBoardId, CreateTaskGroupRequest createTaskGroupRequest) throws NotFoundException;
    public TaskGroupDetailResponse updateTaskGroup(String taskBoardId, String taskGroupId, UpdateTaskGroupRequest updateTaskGroupRequest) throws NotFoundException;
    public String deleteTaskGroup(String taskBoardId, String taskGroupId) throws NotFoundException;
    public TaskGroupDetailResponse getTaskGroupDetail(String taskBoardId, String taskGroupId) throws NotFoundException;
    public String changePosition(String taskBoardId, String taskGroupId, int newPosition) throws NotFoundException, BadRequestException;
    // get tasks of task groups
}
