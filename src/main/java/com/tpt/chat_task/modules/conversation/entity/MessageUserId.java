package com.tpt.chat_task.modules.conversation.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class MessageUserId {
    private String messageId;
    private String userId;
}
