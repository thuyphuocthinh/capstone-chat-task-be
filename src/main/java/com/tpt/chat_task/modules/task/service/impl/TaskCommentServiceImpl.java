package com.tpt.chat_task.modules.task.service.impl;

import com.tpt.chat_task.common.constant.ErrorConstant;
import com.tpt.chat_task.common.constant.Metadata;
import com.tpt.chat_task.common.dto.SuccessResponseWithMetadata;
import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.common.exceptions.ForbiddenException;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.infrastructure.rabbitmq.dto.RabbitMQRequest;
import com.tpt.chat_task.infrastructure.rabbitmq.enums.EXCHANGE_TYPE;
import com.tpt.chat_task.infrastructure.rabbitmq.enums.PUSH_NOTIFICATION_TYPE;
import com.tpt.chat_task.infrastructure.rabbitmq.utils.PushNotificationAction;
import com.tpt.chat_task.infrastructure.rabbitmq.utils.RabbitMQSchema;
import com.tpt.chat_task.modules.auth.constant.AuthError;
import com.tpt.chat_task.modules.auth.jwt.JwtProvider;
import com.tpt.chat_task.modules.notification.constant.NotificationConstant;
import com.tpt.chat_task.modules.notification.constant.NotificationError;
import com.tpt.chat_task.modules.notification.entity.Notification;
import com.tpt.chat_task.modules.notification.enums.NOTIFICATION_TYPE;
import com.tpt.chat_task.modules.notification.repository.NotificationRepository;
import com.tpt.chat_task.modules.resource.entity.Resource;
import com.tpt.chat_task.modules.resource.service.ResourceService;
import com.tpt.chat_task.modules.task.constant.TaskCommentError;
import com.tpt.chat_task.modules.task.constant.TaskError;
import com.tpt.chat_task.modules.task.dto.request.CreateTaskCommentRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateTaskCommentRequest;
import com.tpt.chat_task.modules.task.dto.response.TaskCommentReplyResponse;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
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

    private final RabbitTemplate rabbitTemplate;

    private final ExecutorService executorService;

    private final NotificationRepository notificationRepository;

    private final ResourceService resourceService;

    public TaskComment buildTaskComment(CreateTaskCommentRequest createTaskCommentRequest, Task task, User user, List<Resource> resources) {
        TaskComment taskComment = new TaskComment();
        String content = createTaskCommentRequest.getContent();
        List<String> mentions = createTaskCommentRequest.getMentions();
        if(resources != null && !resources.isEmpty()) {
            List<String> resourceLinks = resources.stream().map(Resource::getLink).collect(Collectors.toList());
            taskComment.setResources(resourceLinks);
        }
        if(mentions != null && !mentions.isEmpty()) {
            taskComment.setMentions(mentions);
        }
        taskComment.setContent(content);
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
                .createdAt(taskComment.getCreatedAt())
                .build();
    }

    @Override
    public TaskCommentResponse addComment(String token, String taskId, CreateTaskCommentRequest createTaskCommentRequest) throws NotFoundException, IOException {
        String userId = this.jwtProvider.getIdFromToken(token);
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        List<Resource> resources = new ArrayList<>();
        if(createTaskCommentRequest.getFiles() != null && createTaskCommentRequest.getFiles().size() > 0) {
            List<MultipartFile> files = createTaskCommentRequest.getFiles();
            resources = this.resourceService.uploadMultipleFiles(files);
        }
        TaskComment taskComment = this.buildTaskComment(createTaskCommentRequest, task, user, resources);
        taskComment = this.taskCommentRepository.save(taskComment);
        TaskCommentResponse taskCommentResponse = this.mapTaskCommentToTaskCommentResponse(taskComment);
        this.pushToQueueAsync(taskCommentResponse, PushNotificationAction.COMMENT_TASK, taskId);
        return taskCommentResponse;
    }

    private void pushToQueueAsync(TaskCommentResponse taskCommentResponse, String pushNotificationAction, String taskId) {
        executorService.execute(() -> {
            this.rabbitTemplate.convertAndSend(
                    RabbitMQSchema.TASK_EXCHANGE,
                    RabbitMQSchema.getTaskRoutingKeyByUserId(taskId),
                    this.buildRabbitRequest(
                            RabbitMQSchema.TASK_ROUTING_KEY,
                            taskCommentResponse,
                            pushNotificationAction,
                            PUSH_NOTIFICATION_TYPE.TASK
                    )
            );
            if(taskCommentResponse.getMentions() != null && !taskCommentResponse.getMentions().isEmpty()) {
                List<String> mentions = taskCommentResponse.getMentions();
                for(String mentionUserId : mentions) {
                    this.pushToNotificationQueueAndDatabase(taskCommentResponse, mentionUserId);
                }
            }
        });
    }

    private void pushToNotificationQueueAndDatabase(TaskCommentResponse taskCommentResponse, String userId) {
        RabbitMQRequest payload = this.buildRabbitRequest(
                RabbitMQSchema.NOTIFICATION_ROUTING_KEY,
                taskCommentResponse,
                PushNotificationAction.NEW_NOTIFICATION,
                PUSH_NOTIFICATION_TYPE.NOTIFICATION
        );
        payload.setUserId(userId);
        payload.setNotificationTitle(NotificationConstant.NOTIFICATION_TITLE);
        payload.setNotificationType(NOTIFICATION_TYPE.MENTION);
        this.rabbitTemplate.convertAndSend(
                RabbitMQSchema.NOTIFICATION_EXCHANGE,
                RabbitMQSchema.NOTIFICATION_ROUTING_KEY,
                payload
        );
    }

    private List<String> getMentionUserIds(TaskComment taskComment) {
        return taskComment.getMentions().stream().map(id -> id).collect(Collectors.toList());
    }

    private List<String> getMembersIdOfTask(Task task) {
        return task.getUsers().stream().map(User::getId).collect(Collectors.toList());
    }

    private RabbitMQRequest buildRabbitRequest(String routingKey, TaskCommentResponse response, String action, PUSH_NOTIFICATION_TYPE pushNotificationType) {
        return RabbitMQRequest.builder()
                .exchangeType(EXCHANGE_TYPE.TOPIC)
                .routingKey(routingKey)
                .payload(response)
                .pushNotificationAction(action)
                .pushNotificationType(pushNotificationType)
                .build();
    }

    @Override
    public TaskCommentResponse updateComment(String token, String taskId, String taskCommentId, UpdateTaskCommentRequest updateTaskCommentRequest) throws NotFoundException, IOException {
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        TaskComment taskComment = this.taskCommentRepository.findById(taskCommentId).orElseThrow(() -> new NotFoundException(TaskCommentError.TASK_COMMENT_NOT_FOUND));

        String userId = this.jwtProvider.getIdFromToken(token);
        if(!taskComment.getSender().getId().equals(userId)) {
            throw new ForbiddenException(ErrorConstant.NOT_ALLOWED_TO_ACCESS_RESOURCE);
        }

        List<Resource> resources = new ArrayList<>();
        if(updateTaskCommentRequest.getFiles() != null && updateTaskCommentRequest.getFiles().size() > 0) {
            List<MultipartFile> files = updateTaskCommentRequest.getFiles();
            resources = this.resourceService.uploadMultipleFiles(files);
        }

        if(resources.size() > 0) {
            List<String> resourceLinks = resources.stream().map(Resource::getLink).collect(Collectors.toList());
            taskComment.setResources(resourceLinks);
        }

        taskComment.setContent(updateTaskCommentRequest.getContent());
        taskComment.setMentions(updateTaskCommentRequest.getMentions());
        taskComment = this.taskCommentRepository.save(taskComment);
        TaskCommentResponse taskCommentResponse = this.mapTaskCommentToTaskCommentResponse(taskComment);
        this.pushToQueueAsync(taskCommentResponse, PushNotificationAction.UPDATE_COMMENT, taskId);
        return taskCommentResponse;
    }

    @Override
    public String deleteComment(String token, String taskId, String taskCommentId) throws NotFoundException {
        this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        TaskComment taskComment = this.taskCommentRepository.findById(taskCommentId).orElseThrow(() -> new NotFoundException(TaskCommentError.TASK_COMMENT_NOT_FOUND));

        String userId = this.jwtProvider.getIdFromToken(token);
        if(!taskComment.getSender().getId().equals(userId)) {
            throw new ForbiddenException(ErrorConstant.NOT_ALLOWED_TO_ACCESS_RESOURCE);
        }

        this.taskCommentRepository.deleteById(taskCommentId);
        TaskCommentResponse taskCommentResponse = this.mapTaskCommentToTaskCommentResponse(taskComment);
        this.pushToQueueAsync(taskCommentResponse, PushNotificationAction.DELETE_COMMENT, taskId);

        executorService.execute(() -> {
            Notification notification = this.notificationRepository.findReactionNotification(
                    NOTIFICATION_TYPE.MENTION,
                    taskId,
                    taskComment.getSender().getId()
            ).orElseThrow(() -> new NotFoundException(NotificationError.NOTIFICATION_NOT_FOUND));
            rabbitTemplate.convertAndSend(
                    RabbitMQSchema.NOTIFICATION_DELETE_EXCHANGE,
                    RabbitMQSchema.NOTIFICATION_DELETE_ROUTING_KEY,
                    notification.getId()
            );
        });

        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    @Transactional
    public TaskCommentResponse replyComment(String token, String taskId, String taskCommentParentId, CreateTaskCommentRequest createTaskCommentRequest) throws NotFoundException, IOException {
        String userId = this.jwtProvider.getIdFromToken(token);
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        TaskComment taskCommentParent = this.taskCommentRepository.findById(taskCommentParentId).orElseThrow(() -> new NotFoundException(TaskCommentError.TASK_COMMENT_NOT_FOUND));

        List<Resource> resources = new ArrayList<>();
        if(createTaskCommentRequest.getFiles() != null && createTaskCommentRequest.getFiles().size() > 0) {
            List<MultipartFile> files = createTaskCommentRequest.getFiles();
            resources = this.resourceService.uploadMultipleFiles(files);
        }

        if(!taskCommentParent.isThreadRoot()) {
            taskCommentParent.setThreadRoot(true);
        }

        TaskComment newTaskComment = this.buildTaskComment(createTaskCommentRequest, task, user, resources);
        newTaskComment.setParentId(taskCommentParent.getId());
        newTaskComment = this.taskCommentRepository.save(newTaskComment);
        this.taskCommentRepository.save(taskCommentParent);
        TaskCommentResponse taskCommentResponse = this.mapTaskCommentToTaskCommentResponse(newTaskComment);
        this.pushToQueueAsync(taskCommentResponse, PushNotificationAction.COMMENT_TASK, taskId);
        return taskCommentResponse;
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
    @Transactional
    public SuccessResponseWithMetadata getThreadComments(String workspaceId, Integer page, Integer paging) throws NotFoundException {
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));

        Pageable pageable =  PageRequest.of(Math.max(0, page - 1), paging);
        Page<TaskComment> threadRootPage = taskCommentRepository.findThreadRootsByWorkspace(workspaceId, pageable);
        List<TaskComment> threadRoots = threadRootPage.getContent();

        List<String> threadRootIds = threadRoots.stream()
                .map(TaskComment::getId)
                .collect(Collectors.toList());

        List<TaskComment> replies = threadRootIds.isEmpty()
                ? List.of()
                : taskCommentRepository.findRepliesByParentIds(threadRootIds);

        Map<String, List<TaskComment>> replyMap = replies.stream()
                .collect(Collectors.groupingBy(TaskComment::getParentId));

        List<TaskCommentReplyResponse> responses = new ArrayList<>();

        for (TaskComment root : threadRoots) {
            List<TaskComment> children = replyMap.getOrDefault(root.getId(), new ArrayList<>());

            List<TaskCommentResponse> replyResponses = children.stream()
                    .map(this::mapTaskCommentToTaskCommentResponse)
                    .collect(Collectors.toList());

            TaskCommentReplyResponse replyResponse = TaskCommentReplyResponse.builder()
                    .id(root.getId())
                    .content(root.getContent())
                    .mentions(root.getMentions())
                    .files(root.getResources())
                    .senderId(root.getSender().getId())
                    .createdAt(root.getCreatedAt())
                    .taskCommentResponses(replyResponses)
                    .build();

            responses.add(replyResponse);
        }



        Metadata metadata = Metadata.builder()
                .currentPage(threadRootPage.getNumber() + 1)
                .totalPages(threadRootPage.getTotalPages())
                .totalElements((int) threadRootPage.getTotalElements())
                .pageSize(threadRootPage.getSize())
                .build();

        return SuccessResponseWithMetadata.builder()
                .metadata(metadata)
                .data(responses)
                .build();
    }
}
