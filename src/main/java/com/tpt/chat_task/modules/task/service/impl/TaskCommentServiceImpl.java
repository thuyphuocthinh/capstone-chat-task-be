package com.tpt.chat_task.modules.task.service.impl;

import com.tpt.chat_task.common.constant.Metadata;
import com.tpt.chat_task.common.dto.SuccessResponseWithMetadata;
import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.auth.jwt.JwtProvider;
import com.tpt.chat_task.modules.conversation.entity.Message;
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
import com.tpt.chat_task.modules.user.constant.UserError;
import com.tpt.chat_task.modules.user.entity.User;
import com.tpt.chat_task.modules.user.repository.UserRepository;
import com.tpt.chat_task.modules.workspace.constant.WorkspaceError;
import com.tpt.chat_task.modules.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO: ADD QUEUE + REALTIME
@Service
@RequiredArgsConstructor
public class TaskCommentServiceImpl implements TaskCommentService {
    private final TaskCommentRepository taskCommentRepository;

    private final TaskRepository taskRepository;

    private final JwtProvider jwtProvider;

    private final WorkspaceRepository workspaceRepository;

    private final UserRepository userRepository;

    public TaskComment buildTaskComment(CreateTaskCommentRequest createTaskCommentRequest, Task task, User user) {
        TaskComment taskComment = new TaskComment();
        String content = createTaskCommentRequest.getContent();
        List<String> mentions = createTaskCommentRequest.getMentions();
        List<String> resourceLinks = createTaskCommentRequest.getResourceLinks();
        taskComment.setContent(content);
        taskComment.setMentions(mentions);
        taskComment.setResources(resourceLinks);
        taskComment.setTask(task);
        taskComment.setSender(user);
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
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        TaskComment taskComment = this.buildTaskComment(createTaskCommentRequest, task, user);
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
    public TaskCommentResponse replyComment(String token, String taskId, String taskCommentParentId, CreateTaskCommentRequest createTaskCommentRequest) throws NotFoundException {
        String userId = this.jwtProvider.getIdFromToken(token);
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        TaskComment taskCommentParent = this.taskCommentRepository.findById(taskCommentParentId).orElseThrow(() -> new NotFoundException(TaskCommentError.TASK_COMMENT_NOT_FOUND));
        TaskComment newTaskComment = this.buildTaskComment(createTaskCommentRequest, task, user);
        newTaskComment.setParentId(taskCommentParent.getId());
        newTaskComment = this.taskCommentRepository.save(newTaskComment);
        return this.mapTaskCommentToTaskCommentResponse(newTaskComment);
    }

    @Override
    public SuccessResponseWithMetadata getListOfCommentsByTask(String taskId, Integer page, Integer paging) throws NotFoundException {
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        Pageable pageable =  PageRequest.of(Math.max(0, page - 1), paging);
        Page<TaskComment> taskCommentPage = this.taskCommentRepository.findAllCommentsByTask(taskId, pageable);
        List<TaskComment> taskComments = taskCommentPage.getContent();

        List<TaskCommentResponse> taskCommentResponses = taskComments.stream().map( taskComment -> this.mapTaskCommentToTaskCommentResponse(taskComment)).collect(Collectors.toList());

        Metadata metadata = Metadata.builder()
                .currentPage(taskCommentPage.getNumber() + 1)
                .totalPages(taskCommentPage.getTotalPages())
                .totalElements((int) taskCommentPage.getTotalElements())
                .pageSize(taskCommentPage.getSize())
                .build();

        return SuccessResponseWithMetadata.builder()
                .metadata(metadata)
                .data(taskCommentResponses)
                .build();
    }

    @Override
    public SuccessResponseWithMetadata getListOfReplyCommentsByTask(String taskId, String taskCommentParentId, Integer page, Integer paging) throws NotFoundException {
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        TaskComment taskCommentParent = this.taskCommentRepository.findById(taskCommentParentId).orElseThrow(() -> new NotFoundException(TaskCommentError.TASK_COMMENT_NOT_FOUND));
        Pageable pageable =  PageRequest.of(Math.max(0, page - 1), paging);

        Page<TaskComment> taskCommentPage = this.taskCommentRepository.findAllReplyCommentsByTaskAndCommentParent(taskId, taskCommentParentId, pageable);
        List<TaskComment> taskComments = taskCommentPage.getContent();

        List<TaskCommentResponse> taskCommentResponses = taskComments.stream().map( taskComment -> this.mapTaskCommentToTaskCommentResponse(taskComment)).collect(Collectors.toList());

        Metadata metadata = Metadata.builder()
                .currentPage(taskCommentPage.getNumber() + 1)
                .totalPages(taskCommentPage.getTotalPages())
                .totalElements((int) taskCommentPage.getTotalElements())
                .pageSize(taskCommentPage.getSize())
                .build();

        return SuccessResponseWithMetadata.builder()
                .metadata(metadata)
                .data(taskCommentResponses)
                .build();
    }

    @Override
    public SuccessResponseWithMetadata getThreadComments(String workspaceId, Integer page, Integer paging) throws NotFoundException {
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));

        Pageable pageable =  PageRequest.of(Math.max(0, page - 1), paging);
        Page<TaskComment> taskCommentPage = this.taskCommentRepository.findAllTaskCommentsByWorkspace(workspaceId, pageable);
        List<TaskComment> taskComments = taskCommentPage.getContent();

        Map<String, List<TaskComment>> threads = taskComments.stream()
                .collect(Collectors.groupingBy(tc -> tc.getParentId() == null ? tc.getId() : tc.getParentId()));

        List<TaskCommentResponse> taskCommentResponses = new ArrayList<>();

        for(Map.Entry<String, List<TaskComment>> entry : threads.entrySet()) {
            List<TaskComment> commentsInThread = entry.getValue();
            for(TaskComment taskComment : commentsInThread) {
                taskCommentResponses.add(this.mapTaskCommentToTaskCommentResponse(taskComment));
            }
        }

        Metadata metadata = Metadata.builder()
                .currentPage(taskCommentPage.getNumber() + 1)
                .totalPages(taskCommentPage.getTotalPages())
                .totalElements((int) taskCommentPage.getTotalElements())
                .pageSize(taskCommentPage.getSize())
                .build();

        return SuccessResponseWithMetadata.builder()
                .metadata(metadata)
                .data(taskCommentResponses)
                .build();
    }
}
