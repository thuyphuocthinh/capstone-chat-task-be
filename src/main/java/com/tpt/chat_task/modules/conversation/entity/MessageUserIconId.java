package com.tpt.chat_task.modules.conversation.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Data
public class MessageUserIconId implements Serializable {
    private String messageId;
    private String userId;
    private String iconId;

    public MessageUserIconId() {}

    public MessageUserIconId(String messageId, String userId, String iconId) {
        this.messageId = messageId;
        this.userId = userId;
        this.iconId = iconId;
    }

    // Getters and Setters
    // equals() and hashCode() — quan trọng cho composite key!
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageUserIconId)) return false;
        MessageUserIconId that = (MessageUserIconId) o;
        return Objects.equals(messageId, that.messageId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(iconId, that.iconId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, userId, iconId);
    }
}

