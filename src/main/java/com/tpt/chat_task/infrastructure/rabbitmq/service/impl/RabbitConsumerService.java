package com.tpt.chat_task.infrastructure.rabbitmq.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.tpt.chat_task.infrastructure.rabbitmq.dto.RabbitMQRequest;
import com.tpt.chat_task.infrastructure.rabbitmq.dto.conversation.ConversationMemberRequest;
import com.tpt.chat_task.modules.conversation.entity.Conversation;
import com.tpt.chat_task.modules.conversation.enums.CONVERSATION_TYPE;
import com.tpt.chat_task.modules.notification.dto.NotificationRequest;
import com.tpt.chat_task.modules.notification.enums.NOTIFICATION_TYPE;
import com.tpt.chat_task.modules.notification.service.NotificationService;
import com.tpt.chat_task.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class RabbitConsumerService {
    private final CommonEventHandlerImpl commonEventHandler;

    private final NotificationService notificationService;

    @RabbitListener(id = "chat-task-listener", queues = {}, concurrency = "4")
    public void receiver(RabbitMQRequest rabbitMQRequest, Message message) {
        String queueName = message.getMessageProperties().getConsumerQueue();
        log.info("Received general message from rabbit : {}", rabbitMQRequest.toString());
        log.info("Received from queue: {}", queueName);
        try {
            log.info("completed {} task", rabbitMQRequest.toString());
        } catch (Exception e) {
            log.error("Error on running test set");
            log.error("Error message : {}", e.getMessage());
            log.error("Error trace : {}", (Object) e.getStackTrace());
        }
    }

    @RabbitListener(queues = "login_queue", concurrency = "2")
    public void receiveLoginEvent(String userId, Message message) {
        log.info("Received login event from rabbit : {}", userId);
        commonEventHandler.handleLoginEvent(userId);
    }

    @RabbitListener(queues = "notification_queue", concurrency = "2")
    public void receiveNotificationEvent(RabbitMQRequest rabbitMQRequest, Message message) {
        log.info("Received notification event from rabbit");
        String userId = rabbitMQRequest.getUserId();
        String title = rabbitMQRequest.getNotificationTitle();
        NOTIFICATION_TYPE type = rabbitMQRequest.getNotificationType();
        NotificationRequest notificationRequest = new NotificationRequest();
        if(userId != null) {
            notificationRequest.setUserId(rabbitMQRequest.getUserId());
        }
        if(type != null) {
            notificationRequest.setType(rabbitMQRequest.getNotificationType());
        }
        if(title != null) {
            notificationRequest.setTitle(rabbitMQRequest.getNotificationTitle());
        }
        notificationRequest.setData((JsonNode) rabbitMQRequest.getPayload());
        notificationService.saveNotification(notificationRequest);
    }

    @RabbitListener(queues = "conversation_add_member_queue", concurrency = "2")
    public void receiveAddMemberConversationEvent(RabbitMQRequest rabbitMQRequest, Message message) {
        log.info("Received add member conversation event from rabbit");
        ConversationMemberRequest payload = (ConversationMemberRequest) rabbitMQRequest.getPayload();
        if(payload.getType() == CONVERSATION_TYPE.PRIVATE) {
            for(String userId : payload.getUserIds()) {
                this.commonEventHandler.handelAddMemberConversationEvent(userId, payload.getConversationId());
            }
        } else {
            this.commonEventHandler.handelDeleteMemberConversationEvent(payload.getUserId(), payload.getConversationId());
        }
    }

    @RabbitListener(queues = "conversation_delete_member_queue", concurrency = "2")
    public void receiveDeleteMemberConversationEvent(RabbitMQRequest rabbitMQRequest, Message message) {
        log.info("Received delete member conversation event from rabbit");
        ConversationMemberRequest payload = (ConversationMemberRequest) rabbitMQRequest.getPayload();
        this.commonEventHandler.handelDeleteMemberConversationEvent(payload.getUserId(), payload.getConversationId());
    }
}
