package com.tpt.chat_task.modules.task.controller;

import com.tpt.chat_task.common.constant.JwtConstant;
import com.tpt.chat_task.common.dto.SuccessResponse;
import com.tpt.chat_task.common.dto.SuccessResponseWithMessage;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.task.dto.request.CreateTaskBoardRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateTaskBoardRequest;
import com.tpt.chat_task.modules.task.service.LabelService;
import com.tpt.chat_task.modules.task.service.TaskBoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/boards")
@RequiredArgsConstructor
public class TaskBoardController {
    private final TaskBoardService taskBoardService;

    private final LabelService labelService;

    @PostMapping
    public ResponseEntity<?> createNewTaskBoard(
            @PathVariable(name = "workspaceId") String workspaceId,
            @RequestBody @Valid CreateTaskBoardRequest request,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken
    ) {
        String accessToken = bearerToken.substring(7);
        SuccessResponse response = SuccessResponse.builder()
                .data(this.taskBoardService.createTaskBoard(accessToken, workspaceId, request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<?> getDetailTaskBoard(
            @PathVariable(name = "workspaceId") String workspaceId,
            @PathVariable(name = "boardId") String boardId
    ) throws NotFoundException {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.taskBoardService.getTaskBoardDetail(workspaceId, boardId))
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTaskBoards(
            @PathVariable(name = "workspaceId") String workspaceId
    ) {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.taskBoardService.getListTaskBoards(workspaceId))
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("")
    public ResponseEntity<?> getAllTaskBoards(
            @PathVariable(name = "workspaceId") String workspaceId,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken
    ) {
        String accessToken = bearerToken.substring(7);
        SuccessResponse response = SuccessResponse.builder()
                .data(this.taskBoardService.getListTaskBoardsByWorkspaceAndUser(workspaceId, accessToken))
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{boardId}")
    public ResponseEntity<?> updateTaskBoard(
            @PathVariable(name = "workspaceId") String workspaceId,
            @PathVariable(name = "boardId") String boardId,
            @RequestBody UpdateTaskBoardRequest request
    ) {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.taskBoardService.updateTaskBoard(workspaceId, boardId, request))
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<?> deleteTaskBoard(
            @PathVariable(name = "workspaceId") String workspaceId,
            @PathVariable(name = "boardId") String boardId
    ) {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(this.taskBoardService.deleteTaskBoard(workspaceId, boardId))
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{boardId}/toggle-pin")
    public ResponseEntity<?> togglePinTaskBoard(
            @PathVariable(name = "workspaceId") String workspaceId,
            @PathVariable(name = "boardId") String boardId
    ) {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.taskBoardService.togglePinTaskBoard(workspaceId, boardId))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{boardId}/add-member/{memberId}")
    public ResponseEntity<?> addMemberToTaskBoard(
            @PathVariable String workspaceId,
            @PathVariable String memberId,
            @PathVariable String boardId
    ) throws BadRequestException {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.taskBoardService.addMemberToTaskBoard(workspaceId, boardId, memberId))
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{boardId}/remove-member/{memberId}")
    public ResponseEntity<?> removeMemberFromTaskBoard(
            @PathVariable String workspaceId,
            @PathVariable String memberId,
            @PathVariable String boardId
    ) throws BadRequestException {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(this.taskBoardService.deleteMemberFromTaskBoard(workspaceId, boardId, memberId))
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{boardId}/members")
    public ResponseEntity<?> getMembersOfTaskBoard(
            @PathVariable String workspaceId,
            @PathVariable String boardId
    ) throws BadRequestException {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.taskBoardService.getListMembers(workspaceId, boardId))
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{boardId}/labels")
    public ResponseEntity<?> getLabelsOfTaskBoard(
            @PathVariable String boardId
    ) {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.labelService.getAllLabelsByTaskBoard(boardId))
                .build();
        return ResponseEntity.ok(response);
    }
}
