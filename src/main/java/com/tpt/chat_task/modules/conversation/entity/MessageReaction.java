package com.tpt.chat_task.modules.conversation.entity;

import com.tpt.chat_task.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_icons")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageReaction {
    @EmbeddedId
    private MessageUserIconId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("messageId")
    @JoinColumn(name = "message_id")
    private Message message;

    @ManyToOne
    @MapsId("iconId")
    @JoinColumn(name = "icon_id")
    private Icon icon;

    @Column(nullable = false, name = "reacted_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime reactedAt;
}
