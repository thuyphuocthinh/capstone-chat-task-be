package com.tpt.chat_task.modules.conversation.controller;

import com.tpt.chat_task.common.constant.AppConstant;
import com.tpt.chat_task.common.constant.JwtConstant;
import com.tpt.chat_task.common.dto.SuccessResponse;
import com.tpt.chat_task.common.dto.SuccessResponseWithMessage;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.conversation.dto.request.CreatePrivateConversationRequest;
import com.tpt.chat_task.modules.conversation.service.PrivateConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/private-conversations")
@RequiredArgsConstructor
public class PrivateConversationController {
    private final PrivateConversationService privateConversationService;

    @PostMapping
    public ResponseEntity<?> addPrivateConversation(
            @PathVariable String workspaceId,
            @Valid @RequestBody CreatePrivateConversationRequest createPrivateConversationRequest
    ) throws NotFoundException, BadRequestException {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.privateConversationService.createPrivateConversation(workspaceId, createPrivateConversationRequest))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping()
    public ResponseEntity<?> getPrivateConversations(
            @PathVariable String workspaceId,
            @RequestParam(name = "page", required = false, defaultValue = AppConstant.PAGE) Integer page,
            @RequestParam(name = "paging", required = false, defaultValue = AppConstant.PAGING) Integer paging,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken
    ) throws NotFoundException {
        String accessToken = bearerToken.substring(7);
        return ResponseEntity.ok(this.privateConversationService.getListPrivateConversations(workspaceId, accessToken, page, paging));
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<?> getPrivateConversationDetail(
            @PathVariable String workspaceId,
            @PathVariable String conversationId
    ) throws NotFoundException {
        SuccessResponse successResponse = SuccessResponse.builder()
                .data(this.privateConversationService.getPrivateConversationDetail(workspaceId, conversationId))
                .build();
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    @PatchMapping("/{conversationId}/toggle-pin")
    public ResponseEntity<?> togglePinPrivateConversation(
            @PathVariable String workspaceId,
            @PathVariable String conversationId
    ) throws NotFoundException {
        SuccessResponseWithMessage successResponse = SuccessResponseWithMessage.builder()
                .message(this.privateConversationService.togglePinPrivateConversation(workspaceId, conversationId))
                .build();
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<?> deletePrivateConversation(
            @PathVariable String workspaceId,
            @PathVariable String conversationId
    ) throws NotFoundException {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(this.privateConversationService.deletePrivateConversation(workspaceId, conversationId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{conversationId}/members")
    public ResponseEntity<?> getPrivateConversationMembers(
            @PathVariable String workspaceId,
            @PathVariable String conversationId
    ) throws NotFoundException {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.privateConversationService.getMembersOfPrivateConversation(workspaceId, conversationId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
