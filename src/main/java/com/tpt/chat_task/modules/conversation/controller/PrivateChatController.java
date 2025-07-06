package com.tpt.chat_task.modules.conversation.controller;

import com.tpt.chat_task.common.constant.AppConstant;
import com.tpt.chat_task.common.constant.JwtConstant;
import com.tpt.chat_task.common.dto.SuccessResponse;
import com.tpt.chat_task.common.dto.SuccessResponseWithMessage;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.conversation.dto.request.MessageRequest;
import com.tpt.chat_task.modules.conversation.service.ChatService;
import com.tpt.chat_task.modules.resource.enums.RESOURCE_TYPE;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/private-conversations")
@RequiredArgsConstructor
public class PrivateChatController {
    private final ChatService chatService;

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<?> sendMessage(
            @PathVariable String conversationId,
            @Valid @RequestBody MessageRequest request,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken
    ) throws IOException {
        String accessToken = bearerToken.substring(7);
        SuccessResponse response = SuccessResponse.builder()
                .data(this.chatService.addNewMessage(accessToken, conversationId, request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{conversationId}/messages/{messageId}")
    public ResponseEntity<?> getMessageDetail(
            @PathVariable String conversationId,
            @PathVariable String messageId) throws NotFoundException {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.chatService.getMessageDetail(conversationId, messageId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{conversationId}/messages/{messageId}")
    public ResponseEntity<?> updateMessage(
            @PathVariable String conversationId,
            @PathVariable String messageId,
            @Valid @RequestBody MessageRequest request
    ) throws NotFoundException, IOException {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.chatService.updateMessage(conversationId, messageId, request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{conversationId}/messages/{messageId}")
    public ResponseEntity<?> deleteMessage(
            @PathVariable String conversationId,
            @PathVariable String messageId
    ) throws NotFoundException, IOException {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(this.chatService.deleteMessage(conversationId, messageId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{conversationId}/messages/{messageId}/toggle-pin")
    public ResponseEntity<?> togglePinMessage(
            @PathVariable String conversationId,
            @PathVariable String messageId
    ) throws NotFoundException, IOException {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(this.chatService.togglePinMessage(conversationId, messageId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/messages/{messageId}/icons/{iconId}")
    public ResponseEntity<?> toggleReactMessage(
            @PathVariable String messageId,
            @PathVariable String iconId
    ) {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(this.chatService.toggleReactMessage(messageId, iconId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/messages/{messageId}/replies")
    public ResponseEntity<?> addRepliesToMessage(
            @PathVariable String messageId,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken,
            @Valid @RequestBody MessageRequest request
    ) throws IOException {
        String accessToken = bearerToken.substring(7);
        SuccessResponse response = SuccessResponse.builder()
                .data(this.chatService.replyMessage(accessToken, messageId, request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<?> getListMessagesByConversationId(
            @RequestParam(name = "paging", required = false, defaultValue = AppConstant.PAGING) Integer paging,
            @PathVariable String conversationId
    ) {
        return ResponseEntity.ok(this.chatService.getListOfMessages(conversationId, paging));
    }

    @PostMapping("/{conversationId}/mark-read")
    public ResponseEntity<?> markReadMessage(
            @PathVariable String conversationId,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken
    ) {
        String accessToken = bearerToken.substring(7);
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(this.chatService.markReadMessagesByConversation(accessToken, conversationId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{conversationId}/messages/{messageId}/above")
    public ResponseEntity<?> getListMessagesByConversationIdAbove(
            @RequestParam(name = "paging", required = false, defaultValue = AppConstant.PAGING) Integer paging,
            @PathVariable String conversationId,
            @PathVariable String messageId
    ) {
        return ResponseEntity.ok(this.chatService.getListOfMessagesAboveOrBelow(conversationId, messageId, paging, true));
    }

    @GetMapping("/{conversationId}/messages/{messageId}/below")
    public ResponseEntity<?> getListMessagesByConversationIdBelow(
            @RequestParam(name = "paging", required = false, defaultValue = AppConstant.PAGING) Integer paging,
            @PathVariable String conversationId,
            @PathVariable String messageId
    ) {
        return ResponseEntity.ok(this.chatService.getListOfMessagesAboveOrBelow(conversationId, messageId, paging, false));
    }

    @GetMapping("/messages/{messageId}/replies")
    public ResponseEntity<?> getListRepliesOfMessage(
            @RequestParam(name = "paging", required = false, defaultValue = AppConstant.PAGING) Integer paging,
            @PathVariable String messageId
    ) {
        return ResponseEntity.ok(this.chatService.getListRepliesOfMessage(messageId, paging));
    }

    @GetMapping("/messages/{parentId}/replies/{messageId}/above")
    public ResponseEntity<?> getListRepliesMessagesByMessageIdAbove(
            @RequestParam(name = "paging", required = false, defaultValue = AppConstant.PAGING) Integer paging,
            @PathVariable String messageId,
            @PathVariable String parentId
    ) {
        return ResponseEntity.ok(this.chatService.getListRepliesOfMessageAboveOrBelow(parentId, messageId, paging, true));
    }

    @GetMapping("/messages/{parentId}/replies/{messageId}/below")
    public ResponseEntity<?> getListRepliesMessagesByMessageIdBelow(
            @RequestParam(name = "paging", required = false, defaultValue = AppConstant.PAGING) Integer paging,
            @PathVariable String messageId,
            @PathVariable String parentId
    ) {
        return ResponseEntity.ok(this.chatService.getListRepliesOfMessageAboveOrBelow(parentId, messageId, paging, false));
    }

    @GetMapping("/{conversationId}/pinned-messages")
    public ResponseEntity<?> getListPinnedMessages(
            @PathVariable String conversationId
    ) {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.chatService.getPinnedMessagesOfConversation(conversationId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{conversationId}/resources")
    public ResponseEntity<?> getListResourcesOfConversation(
            @PathVariable String conversationId,
            @RequestParam(name = "type", required = false) RESOURCE_TYPE type
    ) throws NotFoundException {
        SuccessResponse response = new SuccessResponse();
        if(type == null) {
            response = SuccessResponse.builder()
                    .data(this.chatService.getListResourcesOfConversation(conversationId))
                    .build();
        } else {
            SuccessResponse.builder()
                    .data(this.chatService.getListResourcesOfConversationAndType(conversationId, type))
                    .build();
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
