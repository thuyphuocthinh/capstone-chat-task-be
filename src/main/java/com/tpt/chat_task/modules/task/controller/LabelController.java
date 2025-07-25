package com.tpt.chat_task.modules.task.controller;

import com.tpt.chat_task.common.dto.SuccessResponse;
import com.tpt.chat_task.common.dto.SuccessResponseWithMessage;
import com.tpt.chat_task.modules.task.dto.request.CreateLabelRequest;
import com.tpt.chat_task.modules.task.dto.request.UpdateLabelRequest;
import com.tpt.chat_task.modules.task.service.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boards/{boardId}/labels")
public class LabelController {
    private final LabelService labelService;

    @PostMapping
    public ResponseEntity<?> addNewLabel(
            @PathVariable String boardId,
            @RequestBody @Valid CreateLabelRequest request
    ) {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.labelService.addNewLabel(boardId, request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<?> getAllLabels(@PathVariable String boardId) {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.labelService.getAllLabelsByTaskBoard(boardId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{labelId}")
    public ResponseEntity<?> updateLabel(
            @PathVariable String boardId,
            @PathVariable String labelId,
            @RequestBody @Valid UpdateLabelRequest request
    ) {
        SuccessResponse response = SuccessResponse.builder()
                .data(this.labelService.updateLabel(boardId, labelId, request))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{labelId}")
    public ResponseEntity<?> deleteLabel(
            @PathVariable String boardId,
            @PathVariable String labelId
    ) {
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(this.labelService.deleteLabel(boardId, labelId))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
