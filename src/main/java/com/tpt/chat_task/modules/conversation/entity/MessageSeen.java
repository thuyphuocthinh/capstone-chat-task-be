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
@Table(name = "message_seen")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageSeen {
    @EmbeddedId
    private MessageUserId id;

    @Column(name = "is_seen", nullable = false)
    private boolean isSeen = false;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @MapsId("messageId")
    @JoinColumn(name = "message_id")
    private Message message;

    @Column(nullable = false, name = "seen_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime seenAt;
}
