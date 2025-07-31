package com.tpt.chat_task.modules.conversation.entity;

import com.tpt.chat_task.modules.conversation.enums.CONVERSATION_TYPE;
import com.tpt.chat_task.modules.conversation.enums.MESSAGE_TYPE;
import com.tpt.chat_task.modules.resource.entity.Resource;
import com.tpt.chat_task.modules.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "messages")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(length = 36, name = "thread_root_id")
    private String threadRootId;

    @Column(length = 36, name = "parent_id")
    private String parentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @NotNull(message = "Message type cannot be null")
    private MESSAGE_TYPE type;

    @Column(nullable = false, name = "is_pinned")
    private boolean isPinned = Boolean.FALSE;

    @Column(nullable = false, name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (type == null) {
            type = MESSAGE_TYPE.TEXT;
        }
    }

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "message")
    private List<MessageElement> messageElements = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            joinColumns = @JoinColumn(name = "message_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "resource_id", nullable = false),
            name = "message_resources"
    )
    private List<Resource> resources;

    @OneToMany(mappedBy = "message")
    private List<MessageSeen> messageSeen = new ArrayList<>();
}
