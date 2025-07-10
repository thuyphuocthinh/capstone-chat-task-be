package com.tpt.chat_task.modules.conversation.entity;

import com.tpt.chat_task.modules.conversation.enums.CONVERSATION_TYPE;
import com.tpt.chat_task.modules.user.entity.User;
import com.tpt.chat_task.modules.workspace.entity.Workspace;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "conversations")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(nullable = false, name = "name")
    @NotBlank(message = "Conversation name cannot be blank")
    @Size(min = 1, max = 255, message = "Conversation name length is min 1 and max 255")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "type", length = 20)
    @NotNull(message = "Conversation type cannot be null")
    private CONVERSATION_TYPE type;

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
            type = CONVERSATION_TYPE.GROUP;
        }
    }

    @ManyToOne
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            joinColumns = @JoinColumn(name = "conversation_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "user_id", nullable = false),
            name = "conversation_users"
    )
    private List<User> users;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "conversation")
    private List<Message> messages = new ArrayList<>();
}
