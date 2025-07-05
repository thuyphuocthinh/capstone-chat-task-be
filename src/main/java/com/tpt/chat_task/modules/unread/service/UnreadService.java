package com.tpt.chat_task.modules.unread.service;


import com.tpt.chat_task.modules.unread.dto.response.UnreadNotificationResponse;

// count unread notifications
// count unread group conversations
// count unread private conversations
// count unread tasks comments
public interface UnreadService {
    public int countUnreadPublicConversations(String workspaceId, String token);
    public int countUnreadPrivateConversations(String workspaceId, String token);
    public int countUnreadTaskComments(String workspaceId, String token);
    public UnreadNotificationResponse countUnreadNotifications(String workspaceId, String token);
}
