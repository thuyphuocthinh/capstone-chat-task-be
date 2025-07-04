package com.tpt.chat_task.infrastructure.rabbitmq.service.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpt.chat_task.infrastructure.rabbitmq.dto.RabbitMQRequest;
import com.tpt.chat_task.infrastructure.rabbitmq.dto.conversation.ConversationMemberRequest;
import com.tpt.chat_task.infrastructure.websocket.dto.WebSocketResponse;
import com.tpt.chat_task.infrastructure.websocket.utils.WebSocketSchema;
import com.tpt.chat_task.modules.conversation.dto.response.MessageResponse;
import com.tpt.chat_task.modules.conversation.enums.CONVERSATION_TYPE;
import com.tpt.chat_task.modules.notification.dto.NotificationRequest;
import com.tpt.chat_task.modules.notification.enums.NOTIFICATION_TYPE;
import com.tpt.chat_task.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class RabbitConsumerService {
    private final CommonEventHandlerImpl commonEventHandler;

    private final NotificationService notificationService;

    private final ObjectMapper objectMapper;

    private final SimpMessagingTemplate simpMessagingTemplate;

    // receiver general event mainly solves push notification events
    @RabbitListener(id = "chat-listener", queues = {}, concurrency = "4")
    public void receiverChatEvent(RabbitMQRequest rabbitMQRequest, Message message) {
        String queueName = message.getMessageProperties().getConsumerQueue();
        log.info("Received general event message from rabbit queue : {}", queueName);
        try {
            MessageResponse payload = objectMapper.readValue((JsonParser) rabbitMQRequest.getPayload(), MessageResponse.class);
            String[] splits = queueName.split("\\.");
            if(splits.length == 3) {
                String userId = splits[2];
                WebSocketResponse webSocketResponse = WebSocketResponse.builder()
                        .data(payload)
                        .action(rabbitMQRequest.getPushNotificationAction())
                        .type(rabbitMQRequest.getPushNotificationType())
                        .build();
                simpMessagingTemplate.convertAndSendToUser(
                        WebSocketSchema.getWebsocketNotificationQueue(),
                        userId,
                        webSocketResponse
                );
            }
        } catch (Exception e) {
            log.error("Error on running chat listener");
            log.error("Error message : {}", e.getMessage());
            log.error("Error trace : {}", (Object) e.getStackTrace());
        }
    }

    @RabbitListener(id = "task-listener", queues = {}, concurrency = "4")
    public void receiveTaskEvent(RabbitMQRequest rabbitMQRequest, Message message) {
        String queueName = message.getMessageProperties().getConsumerQueue();
        try {
            // listen from queues of task module
        } catch (Exception e) {
            log.error("Error on running task listener");
        }
    }

    @RabbitListener(queues = "login_queue", concurrency = "2")
    public void receiveLoginEvent(String userId, Message message) {
        log.info("Received login event from rabbit to queue: {}", message.getMessageProperties().getConsumerQueue());
        commonEventHandler.handleLoginEvent(userId);
    }

    @RabbitListener(queues = "notification_queue", concurrency = "2")
    public void receiveNotificationEvent(RabbitMQRequest rabbitMQRequest, Message message) {
        log.info("Received notification event from rabbit to queue: {}", message.getMessageProperties().getConsumerQueue());
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
        log.info("Received add member conversation event from rabbit to queue: {}", message.getMessageProperties().getConsumerQueue());
        ConversationMemberRequest payload = objectMapper.convertValue(rabbitMQRequest.getPayload(), ConversationMemberRequest.class);
        if(payload.getType() == CONVERSATION_TYPE.PRIVATE) {
            for(String userId : payload.getUserIds()) {
                this.commonEventHandler.handelAddMemberConversationEvent(userId, payload.getConversationId());
            }
        } else {
            this.commonEventHandler.handelAddMemberConversationEvent(payload.getUserId(), payload.getConversationId());
        }
    }

    @RabbitListener(queues = "conversation_delete_member_queue", concurrency = "2")
    public void receiveDeleteMemberConversationEvent(RabbitMQRequest rabbitMQRequest, Message message) {
        log.info("Received delete member conversation event from rabbit to queue: {}", message.getMessageProperties().getConsumerQueue());
        ConversationMemberRequest payload = objectMapper.convertValue(rabbitMQRequest.getPayload(), ConversationMemberRequest.class);
        this.commonEventHandler.handelDeleteMemberConversationEvent(payload.getUserId(), payload.getConversationId());
    }
}
