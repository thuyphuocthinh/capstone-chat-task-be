package com.tpt.chat_task.modules.workspace.controller;

import com.tpt.chat_task.common.constant.AppConstant;
import com.tpt.chat_task.common.constant.JwtConstant;
import com.tpt.chat_task.common.dto.SuccessResponse;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.conversation.service.ChatService;
import com.tpt.chat_task.modules.unread.service.UnreadService;
import com.tpt.chat_task.modules.workspace.dto.request.AddMemberRequest;
import com.tpt.chat_task.modules.workspace.dto.request.ChangeRoleRequest;
import com.tpt.chat_task.modules.workspace.dto.request.CreateWorkspaceRequest;
import com.tpt.chat_task.modules.workspace.dto.request.UpdateWorkspaceRequest;
import com.tpt.chat_task.modules.workspace.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    private final UnreadService unreadService;

    private final ChatService chatService;

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<?> createWorkspace(@RequestBody CreateWorkspaceRequest createWorkspaceRequest) {
        SuccessResponse response = SuccessResponse.builder()
                .data(workspaceService.createWorkspace(createWorkspaceRequest))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("")
    public ResponseEntity<?> getListWorkspaces(
            @RequestParam(name = "page", required = false, defaultValue = AppConstant.PAGE) Integer page,
            @RequestParam(name = "paging", required = false, defaultValue = AppConstant.PAGING) Integer paging,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken
    ) {
        String accessToken = bearerToken.substring(7);
        return ResponseEntity.ok(this.workspaceService.getListWorkspaces(accessToken, page, paging));
    }

    @GetMapping("/find")
    public ResponseEntity<?> searchListWorkspacesByName(
            @RequestParam(name = "page", required = false, defaultValue = AppConstant.PAGE) Integer page,
            @RequestParam(name = "paging", required = false, defaultValue = AppConstant.PAGING) Integer paging,
            @RequestParam(name = "name", required = true) String name,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken
    ) {
        String accessToken = bearerToken.substring(7);
        return ResponseEntity.ok(this.workspaceService.searchListWorkspaces(accessToken, name, page, paging));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getWorkspaceById(@PathVariable String id, @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken) {
        String accessToken = bearerToken.substring(7);
        SuccessResponse response = SuccessResponse.builder()
                .data(workspaceService.getWorkspaceById(accessToken, id))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateWorkspaceName(@PathVariable String id, @RequestBody @Valid UpdateWorkspaceRequest request) throws NotFoundException {
        SuccessResponse response = SuccessResponse.builder()
                .data(workspaceService.updateWorkspace(id, request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{id}/add-member")
    public ResponseEntity<?> addMember(@PathVariable String id, @RequestBody @Valid AddMemberRequest request) throws NotFoundException {
        SuccessResponse response = SuccessResponse.builder()
                .data(workspaceService.addMemberToWorkspace(id, request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{workspaceId}/remove-member/{memberId}")
    public ResponseEntity<?> removeMember(@PathVariable String workspaceId, @PathVariable String memberId) throws NotFoundException {
        SuccessResponse response = SuccessResponse.builder()
                .data(workspaceService.removeMemberFromWorkspace(workspaceId, memberId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{workspaceId}/members/{memberId}/change-role")
    public ResponseEntity<?> changeRole(@PathVariable String workspaceId, @PathVariable String memberId, @RequestBody @Valid ChangeRoleRequest request) throws NotFoundException, BadRequestException {
        SuccessResponse response = SuccessResponse.builder()
                .data(workspaceService.changeRoleMemberFromWorkspace(workspaceId, memberId, request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{workspaceId}/conversations/count_unread")
    public ResponseEntity<?> countUnreadPublicConversations(
            @PathVariable String workspaceId,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken
    ) {
        String token = bearerToken.substring(7);
        SuccessResponse response = SuccessResponse.builder()
                .data(this.unreadService.countUnreadPublicConversations(workspaceId, token))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{workspaceId}/private-conversations/count_unread")
    public ResponseEntity<?> countUnreadPrivateConversations(
            @PathVariable String workspaceId,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken
    ) {
        String token = bearerToken.substring(7);
        SuccessResponse response = SuccessResponse.builder()
                .data(this.unreadService.countUnreadPrivateConversations(workspaceId, token))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{workspaceId}/notifications/count_unread")
    public ResponseEntity<?> countUnreadNotifications(
            @PathVariable String workspaceId,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken
    ) {
        String token = bearerToken.substring(7);
        SuccessResponse response = SuccessResponse.builder()
                .data(this.unreadService.countUnreadNotifications(workspaceId, token))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{workspaceId}/tasks/comments/count_unread")
    public ResponseEntity<?> countUnreadTaskComments(
            @PathVariable String workspaceId,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken
    ) {
        String token = bearerToken.substring(7);
        SuccessResponse response = SuccessResponse.builder()
                .data(this.unreadService.countUnreadTaskComments(workspaceId, token))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{workspaceId}/threads")
    public ResponseEntity<?> getListThreads(
            @PathVariable String workspaceId,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken,
            @RequestParam(name = "page", required = false, defaultValue = AppConstant.PAGE) Integer page,
            @RequestParam(name = "paging", required = false, defaultValue = AppConstant.PAGING) Integer paging
    ) {
        String token = bearerToken.substring(7);
        return new ResponseEntity<>(this.chatService.getListThreadsOfWorkspace(token, workspaceId, paging, page), HttpStatus.OK);
    }
}
