package com.tpt.chat_task.modules.task.service.impl;

import com.tpt.chat_task.common.dto.SuccessResponseWithMetadata;
import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.auth.jwt.JwtProvider;
import com.tpt.chat_task.modules.task.constant.TaskCommentError;
import com.tpt.chat_task.modules.task.constant.TaskError;
import com.tpt.chat_task.modules.task.dto.request.CreateTaskCommentRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateTaskCommentRequest;
import com.tpt.chat_task.modules.task.dto.response.TaskCommentResponse;
import com.tpt.chat_task.modules.task.entity.Task;
import com.tpt.chat_task.modules.task.entity.TaskComment;
import com.tpt.chat_task.modules.task.repository.TaskCommentRepository;
import com.tpt.chat_task.modules.task.repository.TaskRepository;
import com.tpt.chat_task.modules.task.service.TaskCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

// TODO: ADD QUEUE + REALTIME
@Service
@RequiredArgsConstructor
public class TaskCommentServiceImpl implements TaskCommentService {
    private final TaskCommentRepository taskCommentRepository;

    private final TaskRepository taskRepository;

    private final JwtProvider jwtProvider;

    public TaskComment buildTaskComment(CreateTaskCommentRequest createTaskCommentRequest, Task task) {
        TaskComment taskComment = new TaskComment();
        String content = createTaskCommentRequest.getContent();
        List<String> mentions = createTaskCommentRequest.getMentions();
        List<String> resourceLinks = createTaskCommentRequest.getResourceLinks();
        taskComment.setContent(content);
        taskComment.setMentions(mentions);
        taskComment.setResources(resourceLinks);
        taskComment.setTask(task);
        return taskComment;
    }

    public TaskCommentResponse mapTaskCommentToTaskCommentResponse(TaskComment taskComment) {
        return TaskCommentResponse.builder()
                .id(taskComment.getId())
                .content(taskComment.getContent())
                .mentions(taskComment.getMentions())
                .files(taskComment.getResources())
                .senderId(taskComment.getSender().getId())
                .build();
    }

    @Override
    public TaskCommentResponse addComment(String token, String taskId, CreateTaskCommentRequest createTaskCommentRequest) throws NotFoundException {
        String userId = this.jwtProvider.getIdFromToken(token);
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        TaskComment taskComment = this.buildTaskComment(createTaskCommentRequest, task);
        taskComment = this.taskCommentRepository.save(taskComment);
        return this.mapTaskCommentToTaskCommentResponse(taskComment);
    }

    @Override
    public TaskCommentResponse updateComment(String taskId, String taskCommentId, UpdateTaskCommentRequest updateTaskCommentRequest) throws NotFoundException {
        return null;
    }

    @Override
    public String deleteComment(String taskId, String taskCommentId) throws NotFoundException {
        this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        this.taskCommentRepository.findById(taskCommentId).orElseThrow(() -> new NotFoundException(TaskCommentError.TASK_COMMENT_NOT_FOUND));
        this.taskCommentRepository.deleteById(taskCommentId);
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public TaskCommentResponse replyComment(String taskId, String taskCommentParentId, CreateTaskCommentRequest createTaskCommentRequest) throws NotFoundException {
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        TaskComment taskCommentParent = this.taskCommentRepository.findById(taskCommentParentId).orElseThrow(() -> new NotFoundException(TaskCommentError.TASK_COMMENT_NOT_FOUND));
        TaskComment newTaskComment = this.buildTaskComment(createTaskCommentRequest, task);
        newTaskComment.setParentId(taskCommentParent.getId());
        newTaskComment = this.taskCommentRepository.save(newTaskComment);
        return this.mapTaskCommentToTaskCommentResponse(newTaskComment);
    }

    @Override
    public SuccessResponseWithMetadata getListOfCommentsByTask(String taskId) throws NotFoundException {
        return null;
    }

    @Override
    public SuccessResponseWithMetadata getListOfReplyCommentsByTask(String taskId, String taskCommentParentId) throws NotFoundException {
        return null;
    }

    @Override
    public SuccessResponseWithMetadata getThreadComments(String workspaceId) throws NotFoundException {
        return null;
    }
}
