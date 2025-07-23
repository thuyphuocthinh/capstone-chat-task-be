package com.tpt.chat_task.modules.task.controller;

import com.tpt.chat_task.common.constant.AppConstant;
import com.tpt.chat_task.common.constant.JwtConstant;
import com.tpt.chat_task.common.dto.SuccessResponse;
import com.tpt.chat_task.common.dto.SuccessResponseWithMessage;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.task.dto.request.CreateTaskRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateTaskRequest;
import com.tpt.chat_task.modules.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/group-tasks/{groupTaskId}")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/tasks")
    public ResponseEntity<?> addNewTask(
            @PathVariable String groupTaskId,
            @RequestBody @Valid CreateTaskRequest createTaskRequest,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken
    ) throws NotFoundException {
        String accessToken = bearerToken.substring(7);
        SuccessResponse response = SuccessResponse.builder()
                .data(taskService.addNewTask(accessToken, groupTaskId, createTaskRequest))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<?> getTaskDetail(
            @PathVariable String taskId,
            @PathVariable String groupTaskId
    ) throws NotFoundException {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.taskService.getDetailedTask(groupTaskId, taskId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/tasks/{taskId}")
    public ResponseEntity<?> updateTask(
            @PathVariable String groupTaskId,
            @PathVariable String taskId,
            @RequestBody UpdateTaskRequest updateTaskRequest
    ) throws NotFoundException {
        SuccessResponse response = SuccessResponse.builder()
                .data(taskService.updateTask(groupTaskId, taskId, updateTaskRequest))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<?> deleteTask(
            @PathVariable String groupTaskId,
            @PathVariable String taskId
    ) throws NotFoundException {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(taskService.deleteTask(groupTaskId, taskId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/tasks")
    public ResponseEntity<?> getTasksByGroupId(
            @PathVariable String groupTaskId,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken,
            @RequestParam(name = "page", required = false, defaultValue = AppConstant.PAGE) Integer page,
            @RequestParam(name = "paging", required = false, defaultValue = AppConstant.PAGING) Integer paging
    ) throws NotFoundException {
        String accessToken = bearerToken.substring(7);
        return new ResponseEntity<>(this.taskService.getListTasksByGroupId(accessToken, groupTaskId, page, paging), HttpStatus.OK);
    }

    @PostMapping("/tasks/{taskId}/members/{userId}")
    public ResponseEntity<?> addMemberToTask(
            @PathVariable String taskId,
            @PathVariable String userId
    ) throws NotFoundException, BadRequestException {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(taskService.addMemberToTask(taskId, userId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/tasks/{taskId}/members/{userId}")
    public ResponseEntity<?> removeMemberFromTask(
            @PathVariable String taskId,
            @PathVariable String userId
    ) throws NotFoundException, BadRequestException {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(taskService.removeMemberFromTask(taskId, userId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/tasks/{taskId}/members")
    public ResponseEntity<?> getMembersOfTask(
            @PathVariable String taskId
    ) throws NotFoundException {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.taskService.getMembersOfTask(taskId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/tasks/{taskId}/position")
    public ResponseEntity<?> changePositionInSameGroup(
            @PathVariable String groupTaskId,
            @PathVariable String taskId,
            @RequestParam int newPosition
    ) throws NotFoundException, BadRequestException {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(this.taskService.changeTaskPositionInSameGroup(groupTaskId, taskId, newPosition))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/tasks/{taskId}/move")
    public ResponseEntity<?> changePositionInDifferentGroup(
            @PathVariable String groupTaskId,
            @PathVariable String taskId,
            @RequestParam String newGroupId,
            @RequestParam int newPosition
    ) throws NotFoundException {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(this.taskService.changeTaskPositionInDiffGroup(groupTaskId, taskId, newGroupId, newPosition))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/tasks/{taskId}/labels/{labelId}")
    public ResponseEntity<?> toggleLabelTask(
            @PathVariable String taskId,
            @PathVariable String labelId
    ) throws NotFoundException {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.taskService.toggleLabelTask(taskId, labelId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/tasks/{taskId}/files")
    public ResponseEntity<?> uploadFiles(
            @PathVariable String taskId,
            @RequestParam("files") List<MultipartFile> files
    ) throws NotFoundException, IOException {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.taskService.addNewFiles(taskId, files))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
