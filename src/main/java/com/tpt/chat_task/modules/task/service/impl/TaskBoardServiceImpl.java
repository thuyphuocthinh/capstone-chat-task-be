package com.tpt.chat_task.modules.task.service.impl;

import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.auth.jwt.JwtProvider;
import com.tpt.chat_task.modules.task.constant.TaskBoardError;
import com.tpt.chat_task.modules.task.dto.request.CreateTaskBoardRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateTaskBoardRequest;
import com.tpt.chat_task.modules.task.dto.response.TaskBoardDetailResponse;
import com.tpt.chat_task.modules.task.dto.response.TaskBoardResponse;
import com.tpt.chat_task.modules.task.dto.response.TaskGroupDetailResponse;
import com.tpt.chat_task.modules.task.entity.TaskBoard;
import com.tpt.chat_task.modules.task.entity.TaskGroup;
import com.tpt.chat_task.modules.task.repository.TaskBoardRepository;
import com.tpt.chat_task.modules.task.repository.TaskGroupRepository;
import com.tpt.chat_task.modules.task.service.TaskBoardService;
import com.tpt.chat_task.modules.user.constant.UserError;
import com.tpt.chat_task.modules.user.entity.User;
import com.tpt.chat_task.modules.user.repository.UserRepository;
import com.tpt.chat_task.modules.workspace.constant.WorkspaceError;
import com.tpt.chat_task.modules.workspace.dto.response.WorkspaceMemberResponse;
import com.tpt.chat_task.modules.workspace.entity.Workspace;
import com.tpt.chat_task.modules.workspace.entity.WorkspaceUser;
import com.tpt.chat_task.modules.workspace.entity.WorkspaceUserId;
import com.tpt.chat_task.modules.workspace.enums.WORKSPACE_USER_ROLE;
import com.tpt.chat_task.modules.workspace.repository.WorkspaceRepository;
import com.tpt.chat_task.modules.workspace.repository.WorkspaceUserRepository;
import com.tpt.chat_task.modules.workspace.service.WorkspaceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskBoardServiceImpl implements TaskBoardService {

    private final TaskBoardRepository taskBoardRepository;

    private final WorkspaceRepository workspaceRepository;

    private final WorkspaceUserRepository workspaceUserRepository;

    private final JwtProvider jwtProvider;

    private final UserRepository userRepository;

    private final TaskGroupRepository taskGroupRepository;

    private final WorkspaceService workspaceService;

    @Override
    public TaskBoardDetailResponse createTaskBoard(String token, String workspaceId, CreateTaskBoardRequest request) throws NotFoundException {
        String userId = this.jwtProvider.getIdFromToken(token);
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));

        boolean isMember = user.getRole().equals(WORKSPACE_USER_ROLE.MEMBER);
        if(isMember) {
            throw new BadCredentialsException(TaskBoardError.NOT_ALLOWED_TO_CREATE_TASK_BOARD);
        }

        Workspace workspace = this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        List<User> userList = new ArrayList<>();
        userList.add(user);
        TaskBoard taskBoard = TaskBoard.builder()
                .title(request.getTitle())
                .backgroundUrl(request.getBackgroundImageUrl())
                .workspace(workspace)
                .users(userList)
                .build();
        taskBoard = this.taskBoardRepository.save(taskBoard);
        return mapTaskBoardToResponse(taskBoard);
    }

    private TaskBoardDetailResponse mapTaskBoardToResponse(TaskBoard taskBoard) {
        String workspaceId = taskBoard.getWorkspace().getId();
        return TaskBoardDetailResponse.builder()
                .title(taskBoard.getTitle())
                .id(taskBoard.getId())
                .backgroundImageUrl(taskBoard.getBackgroundUrl())
                .groups(this.getAndMapTaskGroups(taskBoard.getId()))
                .members(
                        taskBoard.getUsers().stream().map(user -> {
                            return WorkspaceMemberResponse.builder()
                                    .id(user.getId())
                                    .avatar(user.getAvatar())
                                    .email(user.getEmail())
                                    .firstName(user.getFirstName())
                                    .lastName(user.getLastName())
                                    .roleInWorkspace(getUserRole(workspaceId, user.getId()))
                                    .build();
                        }).collect(Collectors.toList())
                )
                .build();
    }

    private List<TaskGroupDetailResponse> getAndMapTaskGroups(String taskBoardId) {
        List<TaskGroup> taskGroups = this.taskGroupRepository.findAllByTaskBoard(taskBoardId);
        return taskGroups.stream().map(tg -> {
            return TaskGroupDetailResponse.builder().id(tg.getId()).title(tg.getTitle()).build();
        }).toList();
    }

    private String getUserRole(String workspaceId, String userId) throws NotFoundException {
        WorkspaceUserId workspaceUserId = new WorkspaceUserId(workspaceId, userId);
        WorkspaceUser workspaceUser = this.workspaceUserRepository.findById(workspaceUserId)
                .orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));

        return workspaceUser.getUserRole().toString();
    }

    @Override
    public TaskBoardDetailResponse getTaskBoardDetail(String workspaceId, String taskBoardId) throws NotFoundException {
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        TaskBoard taskBoard = this.taskBoardRepository.findById(taskBoardId).orElseThrow(() -> new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));
        return mapTaskBoardToResponse(taskBoard);
    }

    @Override
    public TaskBoardDetailResponse updateTaskBoard(String workspaceId, String taskBoardId, UpdateTaskBoardRequest request) throws NotFoundException {
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        TaskBoard taskBoard = this.taskBoardRepository.findById(taskBoardId).orElseThrow(() -> new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));

        String title = taskBoard.getTitle();
        String backgroundImageUrl = taskBoard.getBackgroundUrl();

        if(title != null) {
            taskBoard.setTitle(title);
        }

        if(backgroundImageUrl != null) {
            taskBoard.setBackgroundUrl(backgroundImageUrl);
        }

        taskBoard = this.taskBoardRepository.save(taskBoard);

        return mapTaskBoardToResponse(taskBoard);
    }

    @Override
    public String deleteTaskBoard(String workspaceId, String taskBoardId) throws NotFoundException {
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        this.taskBoardRepository.findById(taskBoardId).orElseThrow(() -> new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));
        this.taskBoardRepository.deleteById(taskBoardId);
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public List<TaskBoardDetailResponse> getListTaskBoards(String workspaceId) throws NotFoundException {
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        List<TaskBoard> taskBoards = this.taskBoardRepository.findAllByWorkspaceId(workspaceId);
        List<TaskBoardDetailResponse> taskBoardDetailResponses = taskBoards.stream().map(tb -> {
            return mapTaskBoardToResponse(tb);
        }).toList();
        return taskBoardDetailResponses;
    }

    @Override
    public String togglePinTaskBoard(String workspaceId, String taskBoardId) throws NotFoundException {
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        TaskBoard taskBoard = this.taskBoardRepository.findById(taskBoardId).orElseThrow(() -> new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));
        taskBoard.setPinned(!taskBoard.isPinned());
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    @Transactional
    public TaskBoardDetailResponse addMemberToTaskBoard(String workspaceId, String taskBoardId, String userId) throws NotFoundException, BadRequestException {
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        TaskBoard taskBoard = this.taskBoardRepository.findById(taskBoardId).orElseThrow(() -> new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));

        boolean isMemberWorkspace = this.workspaceService.isMemberOfWorkspace(workspaceId, user.getId());

        if(!isMemberWorkspace) {
            throw new BadRequestException(WorkspaceError.USER_NOT_IN_WORKSPACE);
        }

        boolean isMemberTaskBoard = this.isMemberTaskBoard(taskBoard, user.getId());
        if(isMemberTaskBoard) {
            throw new BadRequestException(TaskBoardError.USER_ALREADY_IN_TASK_BOARD);
        }

        List<User> userList = taskBoard.getUsers();
        userList.add(user);
        taskBoard.setUsers(userList);
        taskBoard = this.taskBoardRepository.save(taskBoard);

        return mapTaskBoardToResponse(taskBoard);
    }

    private boolean isMemberTaskBoard(TaskBoard taskBoard, String userId) throws NotFoundException {
        List<User> userList = taskBoard.getUsers();
        if(userList.isEmpty()) {
            return false;
        }
        for(User user : userList) {
            if(user.getId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public String deleteMemberFromTaskBoard(String workspaceId, String taskBoardId, String userId) throws NotFoundException, BadRequestException {
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        TaskBoard taskBoard = this.taskBoardRepository.findById(taskBoardId).orElseThrow(() -> new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));

        boolean isMemberWorkspace = this.workspaceService.isMemberOfWorkspace(workspaceId, user.getId());

        if(!isMemberWorkspace) {
            throw new BadRequestException(WorkspaceError.USER_NOT_IN_WORKSPACE);
        }

        boolean isMemberTaskBoard = this.isMemberTaskBoard(taskBoard, user.getId());
        if(!isMemberTaskBoard) {
            throw new BadRequestException(TaskBoardError.USER_NOT_IN_TASK_BOARD);
        }

        List<User> userList = taskBoard.getUsers();
        userList.remove(user);
        taskBoard.setUsers(userList);
        taskBoard = this.taskBoardRepository.save(taskBoard);
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public List<WorkspaceMemberResponse> getListMembers(String workspaceId, String taskBoardId) throws NotFoundException {
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        TaskBoard taskBoard = this.taskBoardRepository.findById(taskBoardId).orElseThrow(() -> new NotFoundException(TaskBoardError.TASK_BOARD_NOT_FOUND));

        List<User> userList = taskBoard.getUsers();

        return userList.stream().map(u -> {
            return WorkspaceMemberResponse.builder()
                    .id(u.getId())
                    .email(u.getEmail())
                    .firstName(u.getFirstName())
                    .lastName(u.getLastName())
                    .avatar(u.getAvatar())
                    .roleInWorkspace(this.getUserRole(workspaceId, u.getId()))
                    .build();
        }).toList();
    }

    @Override
    public List<TaskBoardResponse> getListTaskBoardsByWorkspaceAndUser(String workspaceId, String token) throws NotFoundException {
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        String userId = this.jwtProvider.getIdFromToken(token);
        List<TaskBoard> taskBoards = this.taskBoardRepository.findAllByWorkspaceIdAndUserId(workspaceId, userId);
        return taskBoards.stream().map(tb -> {
            return TaskBoardResponse.builder().id(tb.getId()).title(tb.getTitle()).build();
        }).toList();
    }
}
