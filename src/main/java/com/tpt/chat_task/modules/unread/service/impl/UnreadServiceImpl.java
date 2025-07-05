package com.tpt.chat_task.modules.unread.service.impl;

import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.modules.auth.jwt.JwtProvider;
import com.tpt.chat_task.modules.conversation.enums.CONVERSATION_TYPE;
import com.tpt.chat_task.modules.conversation.repository.MessageSeenRepository;
import com.tpt.chat_task.modules.notification.enums.NOTIFICATION_TYPE;
import com.tpt.chat_task.modules.notification.repository.NotificationUserRepository;
import com.tpt.chat_task.modules.unread.dto.response.UnreadNotificationResponse;
import com.tpt.chat_task.modules.unread.service.UnreadService;
import com.tpt.chat_task.modules.workspace.constant.WorkspaceError;
import com.tpt.chat_task.modules.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnreadServiceImpl implements UnreadService {
    private final NotificationUserRepository notificationUserRepository;

    private final MessageSeenRepository messageSeenRepository;

    private final WorkspaceRepository workspaceRepository;

    private final JwtProvider jwtProvider;

    @Override
    public int countUnreadPublicConversations(String workspaceId, String token) {
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        String userId = this.jwtProvider.getIdFromToken(token);
        return this.messageSeenRepository.countUnreadPublicConversations(userId, CONVERSATION_TYPE.GROUP);
    }

    @Override
    public int countUnreadPrivateConversations(String workspaceId, String token) {
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        String userId = this.jwtProvider.getIdFromToken(token);
        return this.messageSeenRepository.countUnreadPublicConversations(userId, CONVERSATION_TYPE.PRIVATE);
    }

    @Override
    public int countUnreadTaskComments(String workspaceId, String token) {
        return 0;
    }

    @Override
    public UnreadNotificationResponse countUnreadNotifications(String workspaceId, String token) {
        this.workspaceRepository.findById(workspaceId).orElseThrow(() -> new NotFoundException(WorkspaceError.WORKSPACE_NOT_FOUND));
        String userId = this.jwtProvider.getIdFromToken(token);
        return UnreadNotificationResponse.builder()
                .countUnreadAll(this.notificationUserRepository.countUnreadAllNotifications(userId))
                .countUnreadReact(this.notificationUserRepository.countUnreadTypeNotifications(userId, NOTIFICATION_TYPE.REACTION))
                .countUnreadMention(this.notificationUserRepository.countUnreadTypeNotifications(userId, NOTIFICATION_TYPE.MENTION))
                .countUnreadActivities(this.notificationUserRepository.countUnreadTypeNotifications(userId, NOTIFICATION_TYPE.ACTIVITIES))
                .build();
    }
}
