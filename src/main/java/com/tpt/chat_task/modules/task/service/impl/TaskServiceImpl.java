package com.tpt.chat_task.modules.task.service.impl;

import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.infrastructure.rabbitmq.dto.RabbitMQRequest;
import com.tpt.chat_task.infrastructure.rabbitmq.dto.conversation.ConversationMemberRequest;
import com.tpt.chat_task.infrastructure.rabbitmq.dto.task.TaskMemberRequest;
import com.tpt.chat_task.infrastructure.rabbitmq.enums.EXCHANGE_TYPE;
import com.tpt.chat_task.infrastructure.rabbitmq.utils.RabbitMQSchema;
import com.tpt.chat_task.modules.auth.jwt.JwtProvider;
import com.tpt.chat_task.modules.resource.entity.Resource;
import com.tpt.chat_task.modules.resource.service.ResourceService;
import com.tpt.chat_task.modules.task.constant.LabelError;
import com.tpt.chat_task.modules.task.constant.TaskError;
import com.tpt.chat_task.modules.task.constant.TaskGroupError;
import com.tpt.chat_task.modules.task.dto.request.CreateTaskRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateTaskRequest;
import com.tpt.chat_task.modules.task.dto.response.CheckListResponse;
import com.tpt.chat_task.modules.task.dto.response.LabelDetailResponse;
import com.tpt.chat_task.modules.task.dto.response.TaskDetailResponse;
import com.tpt.chat_task.modules.task.dto.response.TaskResourceResponse;
import com.tpt.chat_task.modules.task.entity.CheckList;
import com.tpt.chat_task.modules.task.entity.Label;
import com.tpt.chat_task.modules.task.entity.Task;
import com.tpt.chat_task.modules.task.entity.TaskGroup;
import com.tpt.chat_task.modules.task.repository.LabelRepository;
import com.tpt.chat_task.modules.task.repository.TaskGroupRepository;
import com.tpt.chat_task.modules.task.repository.TaskRepository;
import com.tpt.chat_task.modules.task.service.CheckListService;
import com.tpt.chat_task.modules.task.service.TaskService;
import com.tpt.chat_task.modules.user.constant.UserError;
import com.tpt.chat_task.modules.user.entity.User;
import com.tpt.chat_task.modules.user.repository.UserRepository;
import com.tpt.chat_task.modules.workspace.constant.WorkspaceError;
import com.tpt.chat_task.modules.workspace.dto.response.WorkspaceMemberResponse;
import com.tpt.chat_task.modules.workspace.entity.WorkspaceUser;
import com.tpt.chat_task.modules.workspace.entity.WorkspaceUserId;
import com.tpt.chat_task.modules.workspace.repository.WorkspaceUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    private final TaskGroupRepository taskGroupRepository;

    private final CheckListService checkListService;

    private final WorkspaceUserRepository workspaceUserRepository;

    private final JwtProvider jwtProvider;

    private final UserRepository userRepository;

    private final LabelRepository labelRepository;

    private final ResourceService resourceService;

    private final RabbitTemplate rabbitTemplate;

    @Override
    public TaskDetailResponse addNewTask(String taskGroupId, CreateTaskRequest createTaskRequest) throws NotFoundException {
        TaskGroup taskGroup = this.taskGroupRepository.findById(taskGroupId).orElseThrow(() -> new NotFoundException(TaskGroupError.TASK_GROUP_NOT_FOUND));

        Task task = Task.builder()
                .title(createTaskRequest.getTitle())
                .taskGroup(taskGroup)
                .build();

        task = taskRepository.save(task);

        return mapTaskToResponse(task);
    }

    private List<LabelDetailResponse> mapToLabelDetailResponse(List<Label> labels) {
        return labels.stream().map(l -> {
            return LabelDetailResponse.builder()
                    .id(l.getId())
                    .color(l.getColor())
                    .title(l.getTitle())
                    .build();
        }).toList();
    }

    private List<CheckListResponse> mapCheckListsToCheckListResponse(List<CheckList> checkLists) {
        return checkLists.stream().map(c -> this.checkListService.mapCheckListToResponse(c)).toList();
    }

    private String getUserRole(String workspaceId, String userId) throws NotFoundException {
        WorkspaceUserId workspaceUserId = new WorkspaceUserId(userId, workspaceId);
        WorkspaceUser workspaceUser = this.workspaceUserRepository.findById(workspaceUserId)
                .orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));

        return workspaceUser.getUserRole().toString();
    }

    private List<TaskResourceResponse> mapFilesToResponse(List<Resource> resources) {
        return resources.stream().map(r -> {
            return TaskResourceResponse.builder()
                    .id(r.getId())
                    .name(r.getName())
                    .link(r.getLink())
                    .type(r.getType())
                    .build();
        }).toList();
    }

    private List<WorkspaceMemberResponse> mapUsersToWorkspaceMemberResponse(List<User> users, String workspaceId) {
        return users.stream().map(user -> {
            return WorkspaceMemberResponse.builder()
                    .id(user.getId())
                    .avatar(user.getAvatar())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .roleInWorkspace(getUserRole(workspaceId, user.getId()))
                    .build();
        }).toList();
    }

    private TaskDetailResponse mapTaskToResponse(Task task) {
        String workspaceId = task.getTaskGroup().getTaskBoard().getWorkspace().getId();
        return TaskDetailResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .labels(task.getLabels() == null ? Collections.emptyList() : this.mapToLabelDetailResponse(task.getLabels()))
                .checklists(task.getChecklists() == null ? Collections.emptyList()
                        : this.mapCheckListsToCheckListResponse(task.getChecklists()))
                .members(task.getUsers() == null ? Collections.emptyList()
                        : this.mapUsersToWorkspaceMemberResponse(task.getUsers(), workspaceId))
                .files(task.getResources() == null ? Collections.emptyList()
                        : this.mapFilesToResponse(task.getResources()))
                .build();
    }

    @Override
    public TaskDetailResponse updateTask(String taskGroupId, String taskId, UpdateTaskRequest updateTaskRequest) throws NotFoundException {
        this.taskGroupRepository.findById(taskGroupId).orElseThrow(() -> new NotFoundException(TaskGroupError.TASK_GROUP_NOT_FOUND));
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));

        String title = updateTaskRequest.getTitle();
        String description = updateTaskRequest.getDescription();
        LocalDateTime startDate = updateTaskRequest.getStartDate();
        LocalDateTime dueDate = updateTaskRequest.getDueDate();

        if(title != null) {
            task.setTitle(title);
        }

        if(description != null) {
            task.setDescription(description);
        }

        if(startDate != null) {
            task.setStartDate(startDate);
        }

        if(dueDate != null) {
            task.setDueDate(dueDate);
        }

        task = taskRepository.save(task);

        return this.mapTaskToResponse(task);
    }

    @Override
    public String deleteTask(String taskGroupId, String taskId) throws NotFoundException {
        this.taskGroupRepository.findById(taskGroupId).orElseThrow(() -> new NotFoundException(TaskGroupError.TASK_GROUP_NOT_FOUND));
        this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        this.taskRepository.deleteById(taskId);
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public List<TaskDetailResponse> getListTasksByGroupId(String token, String taskGroupId) throws NotFoundException {
        String userId = this.jwtProvider.getIdFromToken(token);
        List<Task> tasks = this.taskRepository.findAllByUserIdAndGroupId(userId, taskGroupId);
        return tasks.stream().map(task -> this.mapTaskToResponse(task)).collect(Collectors.toList());
    }

    @Override
    public String addMemberToTask(String taskId, String userId) throws NotFoundException {
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        List<User> users = task.getUsers();
        users.add(user);
        task.setUsers(users);
        this.taskRepository.save(task);

        this.rabbitTemplate.convertAndSend(
                RabbitMQSchema.TASK_ADD_MEMBER_EXCHANGE,
                RabbitMQSchema.TASK_ADD_MEMBER_ROUTING_KEY,
                RabbitMQRequest.builder()
                        .routingKey(RabbitMQSchema.TASK_ADD_MEMBER_ROUTING_KEY)
                        .exchangeType(EXCHANGE_TYPE.DIRECT)
                        .payload(TaskMemberRequest.builder().taskId(taskId).userId(userId).build())
                        .userId(userId)
                        .build()
        );

        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public String removeMemberFromTask(String taskId, String userId) throws NotFoundException {
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        List<User> users = task.getUsers();
        users.remove(user);
        task.setUsers(users);
        this.taskRepository.save(task);

        this.rabbitTemplate.convertAndSend(
                RabbitMQSchema.TASK_DELETE_MEMBER_EXCHANGE,
                RabbitMQSchema.TASK_DELETE_MEMBER_ROUTING_KEY,
                RabbitMQRequest.builder()
                        .routingKey(RabbitMQSchema.TASK_DELETE_MEMBER_ROUTING_KEY)
                        .exchangeType(EXCHANGE_TYPE.DIRECT)
                        .payload(TaskMemberRequest.builder().taskId(taskId).userId(userId).build())
                        .userId(userId)
                        .build()
        );

        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public List<WorkspaceMemberResponse> getMembersOfTask(String taskId) throws NotFoundException {
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        List<User> users = task.getUsers();
        String workspaceId = task.getTaskGroup().getTaskBoard().getWorkspace().getId();
        return users.isEmpty() ? Collections.emptyList() : this.mapUsersToWorkspaceMemberResponse(users, workspaceId);
    }

    @Override
    @Transactional
    public String changeTaskPositionInSameGroup(String taskGroupId, String taskId, int newPosition) throws NotFoundException, BadRequestException {
        TaskGroup taskGroup = this.taskGroupRepository.findById(taskGroupId).orElseThrow(() -> new NotFoundException(TaskGroupError.TASK_GROUP_NOT_FOUND));
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));

        int totalTasks = taskGroup.getTasks().size();
        int oldPosition = task.getOrderIndex();

        if(oldPosition == newPosition) {
            return RESPONSE_STATUS.SUCCESS.toString();
        }

        if(oldPosition > totalTasks || oldPosition < 0) {
            throw new BadRequestException(TaskError.INVALID_TASK_POSITION);
        }

        if(newPosition > oldPosition) {
            this.taskRepository.decrementOrderIndexesInRange(taskId, oldPosition + 1, newPosition);
        }
        else {
            this.taskRepository.incrementOrderIndexesInRange(taskId, newPosition, oldPosition - 1);
        }

        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    @Transactional
    public String changeTaskPositionInDiffGroup(String currentTaskGroupId, String newTaskGroupId, String taskId, int newPosition) throws NotFoundException {
        TaskGroup currentTaskGroup = this.taskGroupRepository.findById(currentTaskGroupId)
                .orElseThrow(() -> new NotFoundException(TaskGroupError.TASK_GROUP_NOT_FOUND));
        TaskGroup newTaskGroup = this.taskGroupRepository.findById(newTaskGroupId)
                .orElseThrow(() -> new NotFoundException(TaskGroupError.TASK_GROUP_NOT_FOUND));
        Task task = this.taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));

        int oldPosition = task.getOrderIndex();
        int totalTasksInCurrent = currentTaskGroup.getTasks().size();
        int totalTasksInNew = newTaskGroup.getTasks().size();

        // 1. Giảm orderIndex của các task phía sau ở current group
        this.taskRepository.decrementOrderIndexesInRange(currentTaskGroupId, oldPosition + 1, totalTasksInCurrent);

        // 2. Tăng orderIndex của các task tại new group từ vị trí mới trở đi
        this.taskRepository.incrementOrderIndexesInRange(newTaskGroupId, newPosition, totalTasksInNew);

        // 3. Cập nhật task
        task.setTaskGroup(newTaskGroup);
        task.setOrderIndex(newPosition);
        this.taskRepository.save(task);

        return RESPONSE_STATUS.SUCCESS.toString();
    }

    private boolean checkExistLabels(List<Label> labels, String labelId) {
        for(Label label : labels) {
            if(label.getId().equals(labelId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TaskDetailResponse toggleLabelTask(String taskId, String labelId) throws NotFoundException {
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        Label label = this.labelRepository.findById(labelId).orElseThrow(() -> new NotFoundException(LabelError.LABEL_NOT_FOUND));
        List<Label> labels = task.getLabels();
        boolean existLabel = checkExistLabels(labels, label.getId());
        if(!existLabel) {
            labels.add(label);
        } else {
            labels.remove(label);
        }
        task.setLabels(labels);
        task = taskRepository.save(task);
        return this.mapTaskToResponse(task);
    }

    @Override
    public TaskDetailResponse addNewFiles(String taskId, List<MultipartFile> files) throws NotFoundException, IOException {
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        List<Resource> resources = this.resourceService.uploadMultipleFiles(files);
        task.setResources(resources);
        return this.mapTaskToResponse(task);
    }

    @Override
    public TaskDetailResponse getDetailedTask(String taskGroupId, String taskId) throws NotFoundException {
        this.taskGroupRepository.findById(taskGroupId).orElseThrow(() -> new NotFoundException(TaskGroupError.TASK_GROUP_NOT_FOUND));
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        return this.mapTaskToResponse(task);
    }
}
