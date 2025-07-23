package com.tpt.chat_task.infrastructure.rabbitmq.service.impl;

import com.tpt.chat_task.common.exceptions.NotFoundException;
import com.tpt.chat_task.infrastructure.rabbitmq.dto.RabbitMQRequest;
import com.tpt.chat_task.infrastructure.rabbitmq.service.CommentEventHandler;
import com.tpt.chat_task.infrastructure.rabbitmq.service.RabbitMQService;
import com.tpt.chat_task.infrastructure.rabbitmq.utils.RabbitMQSchema;
import com.tpt.chat_task.infrastructure.websocket.utils.WebSocketSchema;
import com.tpt.chat_task.modules.conversation.constant.ConversationError;
import com.tpt.chat_task.modules.conversation.entity.Conversation;
import com.tpt.chat_task.modules.conversation.enums.CONVERSATION_TYPE;
import com.tpt.chat_task.modules.conversation.repository.ConversationRepository;
import com.tpt.chat_task.modules.task.constant.TaskError;
import com.tpt.chat_task.modules.task.entity.Task;
import com.tpt.chat_task.modules.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class CommonEventHandlerImpl implements CommentEventHandler {
    private final RabbitMQService rabbitMQService;

    private final ConversationRepository conversationRepository;

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final TaskRepository taskRepository;

    @Value("${spring.rabbitmq.listener.id}")
    private String listenerId;

    @Value("${spring.rabbitmq.listener.task-id}")
    private String taskListenerId;

    public void handleLoginEvent(String userId) throws BadRequestException {
        String queueName = RabbitMQSchema.getQueueName(userId);
        String groupExchangeName = RabbitMQSchema.GROUP_CHAT_EXCHANGE;
        String privateExchangeName = RabbitMQSchema.PRIVATE_CHAT_EXCHANGE;

        // get public rooms id
        List<Conversation> publicConversationList = this.conversationRepository.findRoomsByUserIdAndType(userId, CONVERSATION_TYPE.GROUP.name());
        for(Conversation conversation : publicConversationList) {
            String conversationId = conversation.getId();
            this.rabbitMQService.addNewQueue(listenerId, queueName, groupExchangeName, RabbitMQSchema.getGroupChatRoutingKey(conversationId));
            this.rabbitMQService.addNewQueue(listenerId, queueName, groupExchangeName, RabbitMQSchema.getGroupChatAllRoutingKey(conversationId));
            this.rabbitMQService.addNewQueue(listenerId,queueName, groupExchangeName, RabbitMQSchema.getGroupChatMentionRoutingKey(conversationId, userId));
        }

        // get private rooms id
        List<Conversation> privateConversationList = this.conversationRepository.findRoomsByUserIdAndType(userId, CONVERSATION_TYPE.PRIVATE.name());
        for(Conversation conversation : privateConversationList) {
            String conversationId = conversation.getId();
            this.rabbitMQService.addNewQueue(listenerId, queueName, privateExchangeName, RabbitMQSchema.getPrivateChatRoutingKey(conversationId));
            this.rabbitMQService.addNewQueue(listenerId, queueName, privateExchangeName, RabbitMQSchema.getPrivateChatAllRoutingKey(conversationId));
            this.rabbitMQService.addNewQueue(listenerId, queueName, privateExchangeName, RabbitMQSchema.getPrivateChatMentionRoutingKey(conversationId, userId));
        }
    }

    public void handelAddMemberConversationEvent(String userId, String conversationId) throws BadRequestException {
        Conversation conversation = this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        String queueName = RabbitMQSchema.getQueueName(userId);
        if(conversation.getType() == CONVERSATION_TYPE.GROUP) {
            String groupExchangeName = RabbitMQSchema.GROUP_CHAT_EXCHANGE;
            this.rabbitMQService.addNewQueue(listenerId, queueName, groupExchangeName, RabbitMQSchema.getGroupChatRoutingKey(conversationId));
            this.rabbitMQService.addNewQueue(listenerId, queueName, groupExchangeName, RabbitMQSchema.getGroupChatAllRoutingKey(conversationId));
            this.rabbitMQService.addNewQueue(listenerId, queueName, groupExchangeName, RabbitMQSchema.getGroupChatMentionRoutingKey(conversationId, userId));
        } else {
            String privateExchangeName = RabbitMQSchema.PRIVATE_CHAT_EXCHANGE;
            this.rabbitMQService.addNewQueue(listenerId, queueName, privateExchangeName, RabbitMQSchema.getPrivateChatRoutingKey(conversationId));
            this.rabbitMQService.addNewQueue(listenerId, queueName, privateExchangeName, RabbitMQSchema.getPrivateChatAllRoutingKey(conversationId));
            this.rabbitMQService.addNewQueue(listenerId, queueName, privateExchangeName, RabbitMQSchema.getPrivateChatMentionRoutingKey(conversationId, userId));
        }
    }

    public void handelDeleteMemberConversationEvent(String userId, String conversationId) throws BadRequestException {
        Conversation conversation = this.conversationRepository.findById(conversationId).orElseThrow(() -> new NotFoundException(ConversationError.CONVERSATION_NOT_FOUND));
        String queueName = RabbitMQSchema.getQueueName(userId);
        if(conversation.getType() == CONVERSATION_TYPE.GROUP) {
            String groupExchangeName = RabbitMQSchema.GROUP_CHAT_EXCHANGE;
            this.rabbitMQService.unbindQueue(listenerId, queueName, RabbitMQSchema.getGroupChatRoutingKey(conversationId), groupExchangeName);
            this.rabbitMQService.unbindQueue(listenerId, queueName, RabbitMQSchema.getGroupChatAllRoutingKey(conversationId), groupExchangeName);
            this.rabbitMQService.unbindQueue(listenerId, queueName, RabbitMQSchema.getGroupChatMentionRoutingKey(conversationId, userId), groupExchangeName);
        } else {
            String privateExchangeName = RabbitMQSchema.PRIVATE_CHAT_EXCHANGE;
            this.rabbitMQService.unbindQueue(listenerId, queueName, RabbitMQSchema.getPrivateChatRoutingKey(conversationId), privateExchangeName);
            this.rabbitMQService.unbindQueue(listenerId, queueName, RabbitMQSchema.getPrivateChatAllRoutingKey(conversationId), privateExchangeName);
            this.rabbitMQService.unbindQueue(listenerId, queueName, RabbitMQSchema.getPrivateChatMentionRoutingKey(conversationId, userId), privateExchangeName);
        }
    }

    @Override
    public void handleAddMemberTaskEvent(String userId, String taskId) throws NotFoundException, BadRequestException {
        this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        String queueName = RabbitMQSchema.getQueueName(userId);
        this.rabbitMQService.addNewQueue(taskListenerId, queueName, RabbitMQSchema.TASK_EXCHANGE, RabbitMQSchema.getTaskRoutingKeyByUserId(taskId));
    }

    @Override
    public void handleDeleteMemberTaskEvent(String userId, String taskId) throws NotFoundException, BadRequestException {
        Task task = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException(TaskError.TASK_NOT_FOUND));
        String queueName = RabbitMQSchema.getQueueName(userId);
        this.rabbitMQService.unbindQueue(listenerId, queueName, RabbitMQSchema.getTaskRoutingKeyByUserId(taskId), RabbitMQSchema.TASK_EXCHANGE);
    }

    @Override
    public void handleGeneralEvent(RabbitMQRequest rabbitMQRequest) {
        String userId = rabbitMQRequest.getUserId();
        if(userId != null) {
            this.simpMessagingTemplate.convertAndSendToUser(
                    userId,
                    WebSocketSchema.getWebsocketNotificationQueue(),
                    rabbitMQRequest.getPayload()
            );
        }
    }
}
