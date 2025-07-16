package com.tpt.chat_task.modules.conversation.controller;

import com.tpt.chat_task.common.constant.AppConstant;
import com.tpt.chat_task.common.constant.JwtConstant;
import com.tpt.chat_task.common.dto.SuccessResponse;
import com.tpt.chat_task.common.dto.SuccessResponseWithMessage;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.conversation.dto.request.CreateGroupConversationRequest;
import com.tpt.chat_task.modules.conversation.dto.request.UpdateGroupConversationRequest;
import com.tpt.chat_task.modules.conversation.service.ChatService;
import com.tpt.chat_task.modules.conversation.service.GroupConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/group-conversations")
@RequiredArgsConstructor
public class GroupConversationController {
    private final GroupConversationService groupConversationService;

    private final ChatService chatService;

    @PostMapping()
    public ResponseEntity<?> createGroupConversation(@PathVariable String workspaceId, @Valid @RequestBody CreateGroupConversationRequest request) throws NotFoundException {
        SuccessResponse successResponse = SuccessResponse.builder()
                .data(groupConversationService.createNewGroupConversation(workspaceId, request))
                .build();
        return new ResponseEntity<>(successResponse, HttpStatus.CREATED);
    }

    @GetMapping()
    public ResponseEntity<?> getGroupConversations(
            @PathVariable String workspaceId,
            @RequestParam(name = "page", required = false, defaultValue = AppConstant.PAGE) Integer page,
            @RequestParam(name = "paging", required = false, defaultValue = AppConstant.PAGING) Integer paging,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken
    ) throws NotFoundException {
        String accessToken = bearerToken.substring(7);
        return ResponseEntity.ok(this.groupConversationService.getListGroupConversations(workspaceId, accessToken, page, paging));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<?> getGroupConversationDetail(@PathVariable String workspaceId, @PathVariable String conversationId) throws NotFoundException {
        SuccessResponse response = SuccessResponse.builder()
                .data(groupConversationService.getGroupConversationDetail(workspaceId, conversationId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchGroupConversations(
            @PathVariable String workspaceId,
            @RequestParam(name = "page", required = false, defaultValue = AppConstant.PAGE) Integer page,
            @RequestParam(name = "paging", required = false, defaultValue = AppConstant.PAGING) Integer paging,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken,
            @RequestParam(name = "keyword") String keyword
    ) throws NotFoundException {
        String accessToken = bearerToken.substring(7);
        return ResponseEntity.ok(this.groupConversationService.searchListGroupConversations(workspaceId, accessToken, keyword, page, paging));
    }

    @PatchMapping("/{conversationId}")
    public ResponseEntity<?> updateGroupConversation(@PathVariable String workspaceId, @PathVariable String conversationId, @Valid @RequestBody UpdateGroupConversationRequest request) throws NotFoundException {
        SuccessResponse response = SuccessResponse.builder()
                .data(groupConversationService.updateGroupConversation(workspaceId, conversationId, request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<?> deleteGroupConversation(@PathVariable String workspaceId, @PathVariable String conversationId) throws NotFoundException {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(groupConversationService.deleteGroupConversation(workspaceId, conversationId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{conversationId}/toggle-pin")
    public ResponseEntity<?> togglePinGroupConversation(@PathVariable String workspaceId, @PathVariable String conversationId) throws NotFoundException {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(groupConversationService.togglePinnedConversation(workspaceId, conversationId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{conversationId}/members")
    public ResponseEntity<?> getMembers(@PathVariable String workspaceId, @PathVariable String conversationId) throws NotFoundException {
        SuccessResponse response = SuccessResponse.builder()
                .data(groupConversationService.getConversationMembers(workspaceId, conversationId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{conversationId}/add-member/{userId}")
    public ResponseEntity<?> addMember(@PathVariable String workspaceId, @PathVariable String conversationId, @PathVariable String userId) throws NotFoundException, BadRequestException {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(groupConversationService.addMemberToGroupConversation(workspaceId, conversationId, userId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{conversationId}/remove-member/{memberId}")
    public ResponseEntity<?> removeMember(@PathVariable String workspaceId, @PathVariable String conversationId, @PathVariable String memberId) throws NotFoundException, BadRequestException {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(groupConversationService.removeMemberFromGroupConversation(workspaceId, conversationId, memberId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
