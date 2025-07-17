package com.tpt.chat_task.modules.task.controller;

import com.tpt.chat_task.common.dto.SuccessResponse;
import com.tpt.chat_task.modules.task.dto.request.CreateTaskGroupRequest;
import com.tpt.chat_task.modules.task.entity.TaskGroup;
import com.tpt.chat_task.modules.task.service.TaskGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}
