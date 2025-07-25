package com.tpt.chat_task.modules.task.controller;

import com.tpt.chat_task.common.constant.AppConstant;
import com.tpt.chat_task.common.constant.JwtConstant;
import com.tpt.chat_task.common.dto.SuccessResponse;
import com.tpt.chat_task.common.dto.SuccessResponseWithMessage;
import com.tpt.chat_task.modules.task.dto.request.CreateTaskCommentRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateTaskCommentRequest;
import com.tpt.chat_task.modules.task.entity.TaskComment;
import com.tpt.chat_task.modules.task.service.TaskCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController("/api/v1/tasks/{taskId}")
@RequiredArgsConstructor
public class TaskCommentController {
    private final TaskCommentService taskCommentService;

    @PostMapping("/comments")
    public ResponseEntity<?> addComment(
            @PathVariable String taskId,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken,
            @RequestBody @Valid CreateTaskCommentRequest request
    ) throws IOException {
        String accessToken = bearerToken.substring(7);
        SuccessResponse response = SuccessResponse.builder()
                .data(this.taskCommentService.addComment(accessToken, taskId, request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable String taskId,
            @PathVariable String commentId,
            @RequestBody UpdateTaskCommentRequest request
            ) throws IOException {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.taskCommentService.updateComment(taskId, commentId, request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable String taskId,
            @PathVariable String commentId
    ) {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(this.taskCommentService.deleteComment(taskId, commentId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<?> addReplies(
            @PathVariable String taskId,
            @PathVariable String commentId,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken,
            @RequestBody @Valid CreateTaskCommentRequest request
    ) throws IOException {
        String accessToken = bearerToken.substring(7);
        SuccessResponse response = SuccessResponse.builder()
                .data(this.taskCommentService.replyComment(accessToken, taskId, commentId, request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/comments")
    public ResponseEntity<?> getComments(
            @PathVariable String taskId,
            @RequestParam(name = "page", required = false, defaultValue = AppConstant.PAGE) Integer page,
            @RequestParam(name = "paging", required = false, defaultValue = AppConstant.PAGING) Integer paging
    ) {
        return new ResponseEntity<>(this.taskCommentService.getListOfCommentsByTask(taskId, page, paging), HttpStatus.OK);
    }

    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<?> getReplies(
            @PathVariable String taskId,
            @PathVariable String commentId,
            @RequestParam(name = "page", required = false, defaultValue = AppConstant.PAGE) Integer page,
            @RequestParam(name = "paging", required = false, defaultValue = AppConstant.PAGING) Integer paging
    ) {
        return new ResponseEntity<>(this.taskCommentService.getListOfReplyCommentsByTask(taskId, commentId, page, paging), HttpStatus.OK);
    }
}
