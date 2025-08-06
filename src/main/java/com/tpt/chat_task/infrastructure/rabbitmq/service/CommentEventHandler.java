package com.tpt.chat_task.infrastructure.rabbitmq.service;

import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.infrastructure.rabbitmq.dto.RabbitMQRequest;
import org.apache.coyote.BadRequestException;

public interface CommentEventHandler {
    public void handleLoginEvent(String userId) throws BadRequestException;
    public void handelAddMemberConversationEvent(String userId, String conversationId) throws NotFoundException, BadRequestException;
    public void handelDeleteMemberConversationEvent(String userId, String conversationId) throws NotFoundException, BadRequestException;
    public void handleAddMemberTaskEvent(String userId, String taskId) throws NotFoundException, BadRequestException;
    public void handleDeleteMemberTaskEvent(String userId, String taskId) throws NotFoundException, BadRequestException;
    public void handleGeneralEvent(RabbitMQRequest rabbitMQRequest);
}
