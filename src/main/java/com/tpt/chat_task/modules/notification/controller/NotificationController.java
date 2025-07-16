package com.tpt.chat_task.modules.notification.controller;

import com.tpt.chat_task.common.constant.AppConstant;
import com.tpt.chat_task.common.constant.JwtConstant;
import com.tpt.chat_task.common.dto.SuccessResponse;
import com.tpt.chat_task.common.dto.SuccessResponseWithMessage;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.auth.jwt.JwtProvider;
import com.tpt.chat_task.modules.notification.enums.NOTIFICATION_TYPE;
import com.tpt.chat_task.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    private final JwtProvider jwtProvider;

    @GetMapping
    public ResponseEntity<?> getListOfNotificationsByUser(
            @RequestParam(name = "page", required = false, defaultValue = AppConstant.PAGE) Integer page,
            @RequestParam(name = "paging", required = false, defaultValue = AppConstant.PAGING) Integer paging,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken,
            @RequestParam(name = "type", required = false) NOTIFICATION_TYPE type
            ) {
        String accessToken = bearerToken.substring(7);
        String userId = this.jwtProvider.getIdFromToken(accessToken);
        if(type != null){
            return ResponseEntity.ok(this.notificationService.getNotificationsByUserAndType(userId, type, paging, page));
        } else {
            return ResponseEntity.ok(this.notificationService.getNotificationsByUser(userId, paging, page));
        }
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<?> getNotificationById(
            @PathVariable("notificationId") String notificationId,
            @RequestHeader(JwtConstant.JWT_HEADER) String bearerToken
    ) throws NotFoundException {
        String accessToken = bearerToken.substring(7);
        SuccessResponse response = SuccessResponse.builder()
                .data(this.notificationService.getNotificationDetail(accessToken, notificationId))
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/mark-read-all")
    public ResponseEntity<?> markReadAllNotifications(@RequestHeader(JwtConstant.JWT_HEADER) String bearerToken) {
        String accessToken = bearerToken.substring(7);
        SuccessResponseWithMessage response = SuccessResponseWithMessage.builder()
                .message(notificationService.markReadAllNotifications(accessToken))
                .build();
        return ResponseEntity.ok(response);
    }
}
