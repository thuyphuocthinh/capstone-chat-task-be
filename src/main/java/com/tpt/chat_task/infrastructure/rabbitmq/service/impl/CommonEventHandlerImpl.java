package com.tpt.chat_task.infrastructure.rabbitmq.service.impl;

import com.tpt.chat_task.infrastructure.rabbitmq.service.CommentEventHandler;
import com.tpt.chat_task.infrastructure.rabbitmq.service.RabbitMQService;
import com.tpt.chat_task.infrastructure.rabbitmq.utils.RabbitMQSchema;
import com.tpt.chat_task.modules.conversation.entity.Conversation;
import com.tpt.chat_task.modules.conversation.enums.CONVERSATION_TYPE;
import com.tpt.chat_task.modules.conversation.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class CommonEventHandlerImpl implements CommentEventHandler {
    private final RabbitMQService rabbitMQService;

    private final ConversationRepository conversationRepository;

    public void handleLoginEvent(String userId) {
        String queueName = RabbitMQSchema.getQueueName(userId);
        String groupExchangeName = RabbitMQSchema.GROUP_CHAT_EXCHANGE;
        String privateExchangeName = RabbitMQSchema.PRIVATE_CHAT_EXCHANGE;

        // get public rooms id
        List<Conversation> publicConversationList = this.conversationRepository.findRoomsByUserIdAndType(userId, CONVERSATION_TYPE.GROUP.name());
        for(Conversation conversation : publicConversationList) {
            String conversationId = conversation.getId();
            this.rabbitMQService.addNewQueue(queueName, groupExchangeName, RabbitMQSchema.getGroupChatRoutingKey(conversationId));
            this.rabbitMQService.addNewQueue(queueName, groupExchangeName, RabbitMQSchema.getGroupChatAllRoutingKey(conversationId));
            this.rabbitMQService.addNewQueue(queueName, groupExchangeName, RabbitMQSchema.getGroupChatMentionRoutingKey(conversationId, userId));
        }
        // get private rooms id
        List<Conversation> privateConversationList = this.conversationRepository.findRoomsByUserIdAndType(userId, CONVERSATION_TYPE.PRIVATE.name());
        for(Conversation conversation : privateConversationList) {
            String conversationId = conversation.getId();
            this.rabbitMQService.addNewQueue(queueName, privateExchangeName, RabbitMQSchema.getPrivateChatRoutingKey(conversationId));
        }
    }
}
