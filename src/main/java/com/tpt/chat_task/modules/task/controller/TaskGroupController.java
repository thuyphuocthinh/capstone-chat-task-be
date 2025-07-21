package com.tpt.chat_task.modules.task.controller;

import com.tpt.chat_task.common.dto.SuccessResponse;
import com.tpt.chat_task.modules.task.dto.request.CreateTaskGroupRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateTaskGroupRequest;
import com.tpt.chat_task.modules.task.entity.TaskGroup;
import com.tpt.chat_task.modules.task.service.TaskGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boards")
public class TaskGroupController {
    private final TaskGroupService taskGroupService;

    @PostMapping("/{boardId}/group-tasks")
    public ResponseEntity<?> addNewTaskGroup(
            @PathVariable String boardId,
            @RequestBody @Valid CreateTaskGroupRequest request)
    {
        SuccessResponse response = SuccessResponse.builder()
                .data(taskGroupService.addNewTaskGroup(boardId, request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{boardId}/group-tasks/{groupTaskId}")
    public ResponseEntity<?> updateTaskGroup(
            @PathVariable String boardId,
            @PathVariable String groupTaskId,
            @RequestBody UpdateTaskGroupRequest request
    ) {
        SuccessResponse response = SuccessResponse.builder()
                .data(taskGroupService.updateTaskGroup(boardId, groupTaskId, request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{boardId}/group-tasks/{groupTaskId}")
    public ResponseEntity<?> getTaskGroupDetail(
            @PathVariable String boardId,
            @PathVariable String groupTaskId
    ) {
        SuccessResponse response = SuccessResponse.builder()
                .data(taskGroupService.getTaskGroupDetail(boardId, groupTaskId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{boardId}/group-tasks/{groupTaskId}")
    public ResponseEntity<?> updateTaskGroup(
            @PathVariable String boardId,
            @PathVariable String groupTaskId
    ) {
        SuccessResponse response = SuccessResponse.builder()
                .data(taskGroupService.deleteTaskGroup(boardId, groupTaskId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{boardId}/group-tasks/{groupTaskId}/change-position/{newPosition}")
    public ResponseEntity<?> changeTaskGroupPosition(
            @PathVariable String boardId,
            @PathVariable String groupTaskId,
            @PathVariable int newPosition
    ) throws BadRequestException {
        SuccessResponse response = SuccessResponse.builder()
                .data(taskGroupService.changePosition(boardId, groupTaskId, newPosition))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
