package com.tpt.chat_task.modules.task.controller;

import com.tpt.chat_task.common.dto.SuccessResponse;
import com.tpt.chat_task.common.dto.SuccessResponseWithMessage;
import com.tpt.chat_task.modules.task.dto.request.CreateCheckListRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateCheckListRequest;
import com.tpt.chat_task.modules.task.service.CheckListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/checklists")
@RequiredArgsConstructor
public class CheckListController {
    private final CheckListService checkListService;

    @PostMapping
    public ResponseEntity<?> addNewCheckList(
            @PathVariable String taskId,
            @RequestBody @Valid CreateCheckListRequest request
    ) {
        SuccessResponse successResponse = SuccessResponse.builder()
                .data(this.checkListService.addNewCheckList(taskId, request))
                .build();
        return new ResponseEntity<>(successResponse, HttpStatus.CREATED);
    }

    @PatchMapping("/{checklistId}")
    public ResponseEntity<?> updateCheckList(
            @PathVariable String taskId,
            @PathVariable String checklistId,
            @RequestBody @Valid UpdateCheckListRequest request
    ) {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.checkListService.updateNewCheckList(taskId, checklistId, request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{checklistId}")
    public ResponseEntity<?> deleteCheckList(
            @PathVariable String taskId,
            @PathVariable String checklistId
    ) {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.checkListService.deleteCheckList(taskId, checklistId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{checklistId}/items")
    public ResponseEntity<?> getCheckList(
            @PathVariable String taskId,
            @PathVariable String checklistId
    ) {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.checkListService.getCheckListItemByCheckList(checklistId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{checklistId}/change-position/{newPosition}")
    public ResponseEntity<?> changeCheckListPosition(
            @PathVariable String taskId,
            @PathVariable String checklistId,
            @PathVariable int newPosition
    ) throws BadRequestException {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(this.checkListService.changeCheckListPosition(taskId, checklistId, newPosition))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
