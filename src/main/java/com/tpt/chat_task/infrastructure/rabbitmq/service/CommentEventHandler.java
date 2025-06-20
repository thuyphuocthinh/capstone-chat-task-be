package com.tpt.chat_task.infrastructure.rabbitmq.service;

public interface CommentEventHandler {
    public void handleLoginEvent(String userId);
}
