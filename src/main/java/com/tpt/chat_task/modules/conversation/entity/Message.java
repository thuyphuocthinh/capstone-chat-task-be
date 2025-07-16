package com.tpt.chat_task.modules.conversation.entity;

import com.tpt.chat_task.modules.resource.entity.Resource;
import com.tpt.chat_task.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Column(length = 1, name = "is_thread_root")
    private boolean isThreadRoot;

    @Column(length = 36, name = "parent_id")
    private String parentId;

    @Column(nullable = false, name = "is_pinned")
    private boolean isPinned = Boolean.FALSE;

    @Column(nullable = false, name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "message", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageElement> messageElements = new ArrayList<>();

    @OneToMany(mappedBy = "message", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resource> resources = new ArrayList<>();

    @OneToMany(mappedBy = "message", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageSeen> messageSeen = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;
}
