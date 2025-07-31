package com.tpt.chat_task.modules.workspace.service.impl;

import com.tpt.chat_task.common.constant.Metadata;
import com.tpt.chat_task.common.dto.SuccessResponseWithMetadata;
import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.auth.jwt.JwtProvider;
import com.tpt.chat_task.modules.user.constant.UserError;
import com.tpt.chat_task.modules.user.entity.User;
import com.tpt.chat_task.modules.user.enums.USER_ROLE;
import com.tpt.chat_task.modules.user.repository.UserRepository;
import com.tpt.chat_task.modules.workspace.constant.WorkspaceError;
import com.tpt.chat_task.modules.workspace.dto.request.AddMemberRequest;
import com.tpt.chat_task.modules.workspace.dto.request.ChangeRoleRequest;
import com.tpt.chat_task.modules.workspace.dto.request.CreateWorkspaceRequest;
import com.tpt.chat_task.modules.workspace.dto.request.UpdateWorkspaceRequest;
import com.tpt.chat_task.modules.workspace.dto.response.WorkspaceMemberResponse;
import com.tpt.chat_task.modules.workspace.dto.response.WorkspaceResponse;
import com.tpt.chat_task.modules.workspace.entity.Workspace;
import com.tpt.chat_task.modules.workspace.entity.WorkspaceUser;
import com.tpt.chat_task.modules.workspace.entity.WorkspaceUserId;
import com.tpt.chat_task.modules.workspace.enums.WORKSPACE_USER_ROLE;
import com.tpt.chat_task.modules.workspace.repository.WorkspaceRepository;
import com.tpt.chat_task.modules.workspace.repository.WorkspaceUserRepository;
import com.tpt.chat_task.modules.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {
    private final WorkspaceRepository workspaceRepository;

    private final WorkspaceUserRepository workspaceUserRepository;

    private final UserRepository userRepository;

    private final JwtProvider jwtProvider;

    @Override
    public WorkspaceResponse createWorkspace(CreateWorkspaceRequest request) {
        Workspace workspace = new Workspace();
        workspace.setName(request.getName());
        workspace = workspaceRepository.save(workspace);

        User host = this.userRepository.findByRole(USER_ROLE.ADMIN);

        if(host == null) {
            throw new NotFoundException(WorkspaceError.ADMIN_NOT_FOUND);
        }

        WorkspaceUser workspaceUser = new WorkspaceUser();
        workspaceUser.setId(new WorkspaceUserId(host.getId(), workspace.getId()));
        workspaceUser.setWorkspace(workspace);
        workspaceUser.setUser(host);
        workspaceUser.setUserRole(WORKSPACE_USER_ROLE.HOST);
        workspaceUserRepository.save(workspaceUser);

        WorkspaceMemberResponse hostResponse = WorkspaceMemberResponse.builder()
                .email(host.getEmail())
                .firstName(host.getFirstName())
                .lastName(host.getLastName())
                .roleInWorkspace(WORKSPACE_USER_ROLE.HOST.toString())
                .id(host.getId())
                .avatar(host.getAvatar())
                .build();

        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .host(hostResponse)
                .members(new ArrayList<>())
                .build();
    }

    @Override
    public SuccessResponseWithMetadata<?> getListWorkspaces(String token, Integer page, Integer paging) throws NotFoundException {
        String userId = this.jwtProvider.getIdFromToken(token);
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));

        Pageable pageable =  PageRequest.of(Math.max(0, page - 1), paging);
        Page<Workspace> workspacePage = this.workspaceUserRepository.findWorkspacesByUserId(user.getId(), pageable);
        List<Workspace> workspaces = workspacePage.getContent();

        User host = this.userRepository.findByRole(USER_ROLE.ADMIN);
        if(host == null) {
            throw new NotFoundException(WorkspaceError.ADMIN_NOT_FOUND);
        }

        WorkspaceMemberResponse hostResponse = this.findHost();

        List<WorkspaceResponse> workspaceResponses = workspaces.stream().map(workspace ->
                    WorkspaceResponse.builder()
                            .id(workspace.getId())
                            .name(workspace.getName())
                            .host(hostResponse)
                            .members(workspace.getWorkspaceUsers().stream().map(workspaceUser ->
                                        WorkspaceMemberResponse.builder()
                                                .avatar(workspaceUser.getUser().getAvatar())
                                                .email(workspaceUser.getUser().getEmail())
                                                .firstName(workspaceUser.getUser().getFirstName())
                                                .lastName(workspaceUser.getUser().getLastName())
                                                .roleInWorkspace(workspaceUser.getUserRole().toString())
                                                .id(workspaceUser.getUser().getId())
                                                .build()
                                    ).toList())
                            .build()
                ).toList();

        Metadata metadata = Metadata.builder()
                .currentPage(workspacePage.getNumber() + 1)
                .totalPages(workspacePage.getTotalPages())
                .totalElements((int) workspacePage.getTotalElements())
                .pageSize(workspacePage.getSize())
                .build();

        return SuccessResponseWithMetadata.builder()
                .metadata(metadata)
                .data(workspaceResponses)
                .build();
    }

    private WorkspaceMemberResponse findHost() {
        User host = this.userRepository.findByRole(USER_ROLE.ADMIN);
        if(host == null) {
            throw new NotFoundException(WorkspaceError.ADMIN_NOT_FOUND);
        }

        return WorkspaceMemberResponse.builder()
                .email(host.getEmail())
                .firstName(host.getFirstName())
                .lastName(host.getLastName())
                .roleInWorkspace(WORKSPACE_USER_ROLE.HOST.toString())
                .avatar(host.getAvatar())
                .id(host.getId())
                .build();
    }

    @Override
    public SuccessResponseWithMetadata<?> searchListWorkspaces(String token, String name, Integer page, Integer paging) throws NotFoundException {
        String userId = this.jwtProvider.getIdFromToken(token);
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));

        Pageable pageable =  PageRequest.of(Math.max(0, page - 1), paging);
        Page<Workspace> workspacePage = this.workspaceRepository.searchByUserIdAndName(user.getId(), name, pageable);
        List<Workspace> workspaces = workspacePage.getContent();

        WorkspaceMemberResponse hostResponse = this.findHost();

        List<WorkspaceResponse> workspaceResponses = workspaces.stream().map(workspace ->
                WorkspaceResponse.builder()
                        .id(workspace.getId())
                        .name(workspace.getName())
                        .host(hostResponse)
                        .members(workspace.getWorkspaceUsers().stream().map(workspaceUser ->
                                WorkspaceMemberResponse.builder()
                                        .avatar(workspaceUser.getUser().getAvatar())
                                        .email(workspaceUser.getUser().getEmail())
                                        .firstName(workspaceUser.getUser().getFirstName())
                                        .lastName(workspaceUser.getUser().getLastName())
                                        .roleInWorkspace(workspaceUser.getUserRole().toString())
                                        .id(workspaceUser.getUser().getId())
                                        .build()
                        ).toList())
                        .build()
        ).toList();

        Metadata metadata = Metadata.builder()
                .currentPage(workspacePage.getNumber() + 1)
                .totalPages(workspacePage.getTotalPages())
                .totalElements((int) workspacePage.getTotalElements())
                .pageSize(workspacePage.getSize())
                .build();

        return SuccessResponseWithMetadata.builder()
                .metadata(metadata)
                .data(workspaceResponses)
                .build();
    }

    @Override
    public WorkspaceResponse getWorkspaceById(String token, String id) throws NotFoundException {
        String userId = this.jwtProvider.getIdFromToken(token);
        this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));

        Workspace workspace = this.workspaceRepository.findByName(userId, id).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        WorkspaceMemberResponse hostResponse = this.findHost();

        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .host(hostResponse)
                .members(workspace.getWorkspaceUsers().stream().map(workspaceUser ->
                        WorkspaceMemberResponse.builder()
                                .avatar(workspaceUser.getUser().getAvatar())
                                .email(workspaceUser.getUser().getEmail())
                                .firstName(workspaceUser.getUser().getFirstName())
                                .lastName(workspaceUser.getUser().getLastName())
                                .roleInWorkspace(workspaceUser.getUserRole().toString())
                                .id(workspaceUser.getUser().getId())
                                .build()
                ).toList())
                .build();
    }

    @Override
    public WorkspaceResponse updateWorkspace(String id, UpdateWorkspaceRequest request) throws NotFoundException {
        Workspace workspace = this.workspaceRepository.findById(id).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        workspace.setName(request.getName());
        workspace = workspaceRepository.save(workspace);
        WorkspaceMemberResponse hostResponse = this.findHost();
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .host(hostResponse)
                .members(workspace.getWorkspaceUsers().stream().map(workspaceUser ->
                        WorkspaceMemberResponse.builder()
                                .avatar(workspaceUser.getUser().getAvatar())
                                .email(workspaceUser.getUser().getEmail())
                                .firstName(workspaceUser.getUser().getFirstName())
                                .lastName(workspaceUser.getUser().getLastName())
                                .roleInWorkspace(workspaceUser.getUserRole().toString())
                                .id(workspaceUser.getUser().getId())
                                .build()
                ).toList())
                .build();
    }

    @Override
    public String addMemberToWorkspace(String workspaceId, AddMemberRequest request) throws NotFoundException {
        String userId = request.getUserId();
        WORKSPACE_USER_ROLE role = request.getRole();

        Workspace workspace = this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        Optional<Workspace> checkExist = this.workspaceUserRepository.findWorkspaceByWorkspaceIdAndUserId(userId, workspaceId);

        if(checkExist.isPresent()) {
            throw new NotFoundException(WorkspaceError.USER_ALREADY_IN_WORKSPACE);
        }

        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));

        WorkspaceUser workspaceUser = new WorkspaceUser();
        workspaceUser.setId(new WorkspaceUserId(user.getId(), workspace.getId()));
        workspaceUser.setWorkspace(workspace);
        workspaceUser.setUser(user);
        workspaceUser.setUserRole(role);
        workspaceUserRepository.save(workspaceUser);

        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public String removeMemberFromWorkspace(String workspaceId, String userId) throws NotFoundException {
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        Workspace checkExist = this.workspaceUserRepository.findWorkspaceByWorkspaceIdAndUserId(userId, workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.USER_NOT_IN_WORKSPACE));

        WorkspaceUserId id = new WorkspaceUserId(userId, workspaceId);
        WorkspaceUser workspaceUser = this.workspaceUserRepository.findById(id).orElseThrow(() -> new NotFoundException(WorkspaceError.USER_NOT_IN_WORKSPACE));

        this.workspaceUserRepository.delete(workspaceUser);

        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public String changeRoleMemberFromWorkspace(String workspaceId, String userId, ChangeRoleRequest request) throws NotFoundException, BadRequestException {
        this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));

        WorkspaceUserId id = new WorkspaceUserId(userId, workspaceId);
        WorkspaceUser workspaceUser = this.workspaceUserRepository.findById(id).orElseThrow(() -> new NotFoundException(WorkspaceError.USER_NOT_IN_WORKSPACE));

        WORKSPACE_USER_ROLE role = request.getRole();
        if(role == WORKSPACE_USER_ROLE.HOST) {
            throw new BadRequestException(WorkspaceError.INVALID_WORKSPACE_ROLE);
        }

        workspaceUser.setUserRole(role);
        workspaceUserRepository.save(workspaceUser);

        return RESPONSE_STATUS.SUCCESS.toString();
    }
}
