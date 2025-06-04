package com.tpt.chat_task.modules.conversation.entity;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public class MessageUserIconId {
    private String messageId;
    private String userId;
    private String iconId;
}
