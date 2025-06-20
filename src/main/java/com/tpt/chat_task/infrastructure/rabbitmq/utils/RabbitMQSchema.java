package com.tpt.chat_task.infrastructure.rabbitmq.utils;

import org.springframework.beans.factory.annotation.Value;

public class RabbitMQSchema {
    @Value("${spring.rabbitmq.login.exchange}")
    public static String LOGIN_EXCHANGE;

    @Value("${spring.rabbitmq.login.queue}")
    public static String LOGIN_QUEUE;

    @Value("${spring.rabbitmq.login.routing-key}")
    public static String LOGIN_ROUTING_KEY;

    public static final String GROUP_CHAT_EXCHANGE = "group_chat_exchange";

    public static final String PRIVATE_CHAT_EXCHANGE = "private_chat_exchange";

    public static String getGroupChatRoutingKey(String roomId) {
        return "chat.topic.group." + roomId;
    }

    public static String getGroupChatAllRoutingKey(String roomId) {
        return "chat.topic.group." + roomId + ".all";
    }

    public static String getGroupChatMentionRoutingKey(String roomId, String userId) {
        return "chat.topic.group." + roomId + ".mention.user." + userId;
    }

    public static String getPrivateChatRoutingKey(String roomId) {
        return "chat.topic.private." + roomId ;
    }

    public static String getQueueName(String userId) {
        return "chat.queue." + userId;
    }
}

