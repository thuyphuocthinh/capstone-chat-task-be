package com.tpt.chat_task.modules.notification.service.impl;

import com.tpt.chat_task.common.constant.Metadata;
import com.tpt.chat_task.common.dto.SuccessResponseWithMetadata;
import com.tpt.chat_task.common.enums.RESPONSE_STATUS;
import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.infrastructure.rabbitmq.enums.PUSH_NOTIFICATION_TYPE;
import com.tpt.chat_task.infrastructure.websocket.dto.WebSocketResponse;
import com.tpt.chat_task.infrastructure.websocket.utils.WebSocketSchema;
import com.tpt.chat_task.modules.auth.jwt.JwtProvider;
import com.tpt.chat_task.modules.notification.constant.NotificationConstant;
import com.tpt.chat_task.modules.notification.constant.NotificationError;
import com.tpt.chat_task.modules.notification.dto.NotificationDetailResponse;
import com.tpt.chat_task.modules.notification.dto.NotificationRequest;
import com.tpt.chat_task.modules.notification.entity.Notification;
import com.tpt.chat_task.modules.notification.entity.NotificationUser;
import com.tpt.chat_task.modules.notification.entity.NotificationUserId;
import com.tpt.chat_task.modules.notification.enums.NOTIFICATION_TYPE;
import com.tpt.chat_task.modules.notification.repository.NotificationRepository;
import com.tpt.chat_task.modules.notification.repository.NotificationUserRepository;
import com.tpt.chat_task.modules.notification.service.NotificationService;
import com.tpt.chat_task.modules.user.constant.UserError;
import com.tpt.chat_task.modules.user.entity.User;
import com.tpt.chat_task.modules.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    private final NotificationUserRepository notificationUserRepository;

    private final SimpMessagingTemplate messagingTemplate;

    private final UserRepository userRepository;

    private final JwtProvider jwtProvider;

    private final NotificationUserRepository nonNotificationUserRepository;

    @Override
    public void saveNotification(NotificationRequest notificationRequest) {
        User user = this.userRepository.findById(notificationRequest.getUserId()).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));

        Notification notification = new Notification();
        notification.setTitle(notificationRequest.getTitle());
        notification.setData(notificationRequest.getData());
        notification.setType(notificationRequest.getType());
        notification = notificationRepository.save(notification);

        NotificationUser notificationUser = new NotificationUser();
        NotificationUserId notificationUserId = new NotificationUserId(notification.getId(), user.getId());
        notificationUser.setId(notificationUserId);
        notificationUser.setNotification(notification);
        notificationUser.setUser(user);
        notificationUserRepository.save(notificationUser);

        // send notification here by using websocket
        messagingTemplate.convertAndSendToUser(
                user.getId(),
                WebSocketSchema.getWebsocketNotificationQueue(),
                WebSocketResponse.builder()
                        .data(
                                NotificationDetailResponse.builder()
                                        .data(notification.getData())
                                        .userId(user.getId())
                                        .title(notificationRequest.getTitle())
                                        .type(notificationRequest.getType())
                                        .id(notification.getId())
                                        .build()
                        )
                        .action(NotificationConstant.NOTIFICATION_ACTION)
                        .type(PUSH_NOTIFICATION_TYPE.NOTIFICATION)
                        .notificationType(notification.getType())
                        .build()
        );
    }

    @Override
    public void deleteNotification(String id) throws NotFoundException {
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new NotFoundException(NotificationError.NOTIFICATION_NOT_FOUND));
        notificationRepository.delete(notification);
    }

    @Override
    public SuccessResponseWithMetadata<?> getNotificationsByUser(String userId, Integer paging, Integer page) throws NotFoundException {
        log.info("notification user id: {}", userId);
        this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));

        Pageable pageable =  PageRequest.of(Math.max(0, page - 1), paging);
        Page<Notification> notificationPage = this.notificationUserRepository.findNotificationsByUserId(userId, pageable);
        log.info("notifications: {}", notificationPage.toString());

        List<Notification> notifications = notificationPage.getContent();

        List<NotificationDetailResponse> notificationDetailResponses = notifications.stream().map(n -> {
            return NotificationDetailResponse.builder()
                    .id(n.getId())
                    .userId(userId)
                    .title(n.getTitle())
                    .data(n.getData())
                    .type(n.getType())
                    .build();
        }).toList();

        Metadata metadata = Metadata.builder()
                .currentPage(notificationPage.getNumber() + 1)
                .totalPages(notificationPage.getTotalPages())
                .totalElements((int) notificationPage.getTotalElements())
                .pageSize(notificationPage.getSize())
                .build();

        return SuccessResponseWithMetadata.builder()
                .metadata(metadata)
                .data(notificationDetailResponses)
                .build();
    }

    @Override
    public SuccessResponseWithMetadata<?> getNotificationsByUserAndType(String userId, NOTIFICATION_TYPE type, Integer paging, Integer page) throws NotFoundException {
        this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(UserError.USER_NOT_FOUND));

        Pageable pageable =  PageRequest.of(Math.max(0, page - 1), paging);
        Page<Notification> notificationPagePage = this.notificationUserRepository.findNotificationsByUserIdAndType(userId, type, pageable);
        List<Notification> notifications = notificationPagePage.getContent();

        List<NotificationDetailResponse> notificationDetailResponses = notifications.stream().map(n -> {
            return NotificationDetailResponse.builder()
                    .id(n.getId())
                    .userId(userId)
                    .title(n.getTitle())
                    .data(n.getData())
                    .type(n.getType())
                    .build();
        }).toList();

        Metadata metadata = Metadata.builder()
                .currentPage(notificationPagePage.getNumber() + 1)
                .totalPages(notificationPagePage.getTotalPages())
                .totalElements((int) notificationPagePage.getTotalElements())
                .pageSize(notificationPagePage.getSize())
                .build();

        return SuccessResponseWithMetadata.builder()
                .metadata(metadata)
                .data(notificationDetailResponses)
                .build();
    }

    @Override
    @Transactional
    public String markReadAllNotifications(String token) throws NotFoundException {
        String userId = this.jwtProvider.getIdFromToken(token);
        this.notificationUserRepository.markReadAllNotifications(userId);
        return RESPONSE_STATUS.SUCCESS.toString();
    }

    @Override
    public NotificationDetailResponse getNotificationDetail(String token, String id) throws NotFoundException {
        String userId = this.jwtProvider.getIdFromToken(token);
        Notification notification = this.notificationRepository.findById(id).orElseThrow(() -> new NotFoundException(NotificationError.NOTIFICATION_NOT_FOUND));
        this.notificationUserRepository.markReadNotification(userId, notification.getId());
        return NotificationDetailResponse.builder()
                .data(notification.getData())
                .title(notification.getTitle())
                .type(notification.getType())
                .build();
    }
}
