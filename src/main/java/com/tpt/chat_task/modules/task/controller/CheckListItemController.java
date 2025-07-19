package com.tpt.chat_task.modules.task.controller;

import com.tpt.chat_task.common.dto.SuccessResponse;
import com.tpt.chat_task.common.dto.SuccessResponseWithMessage;
import com.tpt.chat_task.modules.task.dto.request.CreateCheckListItemRequest;
import com.tpt.chat_task.modules.task.dto.request.CreateCheckListRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateCheckListItemRequest;
import com.tpt.chat_task.modules.task.service.CheckListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checklists/{checklistId}/items")
@RequiredArgsConstructor
public class CheckListItemController {
    private final CheckListService checkListService;

    @PostMapping
    public ResponseEntity<?> addNewCheckListItem(
            @PathVariable String checklistId,
            @RequestBody @Valid CreateCheckListItemRequest request) {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.checkListService.addNewCheckListItem(checklistId, request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<?> updateCheckListItem(
            @PathVariable String checklistId,
            @PathVariable String itemId,
            @RequestBody UpdateCheckListItemRequest request
    ) {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.checkListService.updateCheckListItem(checklistId, itemId, request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteCheckListItem(
            @PathVariable String checklistId,
            @PathVariable String itemId
    ) {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(checkListService.deleteCheckListItem(checklistId, itemId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{itemId}/change-status")
    public ResponseEntity<?> changeCheckListItemStatus(
            @PathVariable String checklistId,
            @PathVariable String itemId
    ) {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.checkListService.changeCheckListItemStatus(checklistId, itemId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{itemId}/change-position/{newPosition}")
    public ResponseEntity<?> changeCheckListItemPosition(
            @PathVariable String checklistId,
            @PathVariable String itemId,
            @PathVariable int newPosition
    ) throws BadRequestException {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(this.checkListService.changeCheckListItemPosition(checklistId, itemId, newPosition))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
