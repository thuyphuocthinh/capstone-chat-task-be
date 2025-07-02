package com.tpt.chat_task.infrastructure.rabbitmq.service;

import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.infrastructure.rabbitmq.dto.RabbitMQRequest;

public interface CommentEventHandler {
    public void handleLoginEvent(String userId);
    public void handelAddMemberConversationEvent(String userId, String conversationId) throws NotFoundException;
    public void handelDeleteMemberConversationEvent(String userId, String conversationId) throws NotFoundException;
    public void handleGeneralEvent(RabbitMQRequest rabbitMQRequest);
}
