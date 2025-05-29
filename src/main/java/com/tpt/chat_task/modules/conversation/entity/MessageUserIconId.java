package com.tpt.chat_task.modules.conversation.entity;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public class MessageUserIconId {
    private UUID messageId;
    private UUID userId;
    private UUID iconId;
}
