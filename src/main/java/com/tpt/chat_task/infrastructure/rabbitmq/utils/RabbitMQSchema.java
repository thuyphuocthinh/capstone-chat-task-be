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

    public static final String NOTIFICATION_EXCHANGE = "notification_exchange";

    public static final String NOTIFICATION_ROUTING_KEY = "notification_routing_key";

    public static final String NOTIFICATION_QUEUE = "notification_queue";

    public static final String CONVERSATION_ADD_MEMBER_EXCHANGE = "conversation_add_member_exchange";

    public static final String CONVERSATION_ADD_MEMBER_ROUTING_KEY = "conversation_add_member_routing_key";

    public static final String CONVERSATION_ADD_MEMBER_QUEUE = "conversation_add_member_queue";

    public static final String CONVERSATION_DELETE_MEMBER_EXCHANGE = "conversation_delete_member_exchange";

    public static final String CONVERSATION_DELETE_MEMBER_ROUTING_KEY = "conversation_delete_member_routing_key";

    public static final String CONVERSATION_DELETE_MEMBER_QUEUE = "conversation_delete_member_queue";

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

    public static String getPrivateChatAllRoutingKey(String roomId) {
        return "chat.topic.private." + roomId + ".all" ;
    }

    public static String getPrivateChatMentionRoutingKey(String roomId, String userId) {
        return "chat.topic.private." + roomId + ".mention.user." + userId;
    }

    public static String getQueueName(String userId) {
        return "chat.queue." + userId;
    }
}

