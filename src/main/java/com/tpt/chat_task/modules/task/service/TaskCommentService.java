package com.tpt.chat_task.modules.task.service;

import com.tpt.chat_task.common.dto.SuccessResponseWithMetadata;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.task.dto.request.CreateTaskCommentRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateTaskCommentRequest;
import com.tpt.chat_task.modules.task.dto.response.TaskCommentResponse;
import com.tpt.chat_task.modules.task.entity.TaskComment;

public interface TaskCommentService {
    public TaskCommentResponse addComment(String token, String taskId, CreateTaskCommentRequest createTaskCommentRequest) throws NotFoundException;
    public TaskCommentResponse updateComment(String taskId, String taskCommentId, UpdateTaskCommentRequest updateTaskCommentRequest) throws NotFoundException;
    public String deleteComment(String taskId, String taskCommentId) throws NotFoundException;
    public TaskCommentResponse replyComment(String token, String taskId, String taskCommentParentId, CreateTaskCommentRequest createTaskCommentRequest) throws NotFoundException;
    public SuccessResponseWithMetadata getListOfCommentsByTask(String taskId, Integer page, Integer paging) throws NotFoundException;
    public SuccessResponseWithMetadata getListOfReplyCommentsByTask(String taskId, String taskCommentParentId, Integer page, Integer paging) throws NotFoundException;
    public SuccessResponseWithMetadata getThreadComments(String workspaceId, Integer page, Integer paging) throws NotFoundException;
}

