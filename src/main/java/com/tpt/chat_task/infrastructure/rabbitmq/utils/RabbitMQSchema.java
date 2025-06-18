package com.tpt.chat_task.infrastructure.rabbitmq.utils;

public class RabbitMQSchema {
    private final String GROUP_CHAT_EXCHANGE = "group_chat_exchange";

    private final String PRIVATE_CHAT_EXCHANGE = "private_chat_exchange";

    public String getGroupChatRoutingKey(String roomId) {
        return "chat.topic.group." + roomId;
    }

    public String getGroupChatAllRoutingKey(String roomId) {
        return "chat.topic.group." + roomId + ".all";
    }

    public String getGroupChatMentionRoutingKey(String roomId, String userId) {
        return "chat.topic.group." + roomId + ".mention.user." + userId;
    }

    public String getPrivateChatRoutingKey(String roomId) {
        return "chat.topic.private." + roomId ;
    }

    public String getQueueName(String userId) {
        return "chat.queue." + userId;
    }
}

