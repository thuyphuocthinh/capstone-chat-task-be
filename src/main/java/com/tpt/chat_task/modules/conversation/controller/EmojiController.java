package com.tpt.chat_task.modules.conversation.controller;

import com.tpt.chat_task.common.dto.SuccessResponse;
import com.tpt.chat_task.modules.conversation.dto.request.CreateEmojiRequest;
import com.tpt.chat_task.modules.conversation.dto.request.UpdateEmojiRequest;
import com.tpt.chat_task.modules.conversation.service.IconService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/emojis")
@RequiredArgsConstructor
@Validated
public class EmojiController {
    private final IconService iconService;

    @GetMapping()
    public ResponseEntity<?> getAllEmojis() {
        SuccessResponse successResponse = SuccessResponse.builder()
                .data(iconService.getAllEmojis())
                .build();
        return ResponseEntity.ok(successResponse);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping()
    public ResponseEntity<?> addEmojis(@RequestBody @Valid CreateEmojiRequest request) {
        SuccessResponse successResponse = SuccessResponse.builder()
                .data(iconService.createEmoji(request))
                .build();
        return new ResponseEntity<>(successResponse, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/create-multi")
    public ResponseEntity<?> addMultiEmojis(@RequestBody @Valid List<CreateEmojiRequest> requests) {
        SuccessResponse successResponse = SuccessResponse.builder()
                .data(iconService.createEmojis(requests))
                .build();
        return new ResponseEntity<>(successResponse, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateEmojis(@PathVariable("id") String id, @RequestBody @Valid UpdateEmojiRequest request) {
        SuccessResponse successResponse = SuccessResponse.builder()
                .data(iconService.updateEmoji(id, request))
                .build();
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmoji(@PathVariable("id") String id) {
        SuccessResponse successResponse = SuccessResponse.builder()
                .data(iconService.deleteEmoji(id))
                .build();
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }
}
