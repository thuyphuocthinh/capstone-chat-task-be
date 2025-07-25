package com.tpt.chat_task.modules.task.entity;

import com.tpt.chat_task.modules.task.enums.TASK_COMMENT_TYPE;
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
@Table(name = "task_comments")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TaskComment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(length = 36, name = "thread_root_id")
    private String threadRootId;

    @Column(length = 36, name = "parent_id")
    private String parentId;

    @Column(columnDefinition = "TEXT", nullable = false, name = "content")
    private String content;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false, updatable = false)
    private User sender;

    @ElementCollection
    @CollectionTable(name = "task_comment_mentions", joinColumns = @JoinColumn(name = "task_comment_id"))
    @Column(name = "mention")
    private List<String> mentions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "task_comment_resources", joinColumns = @JoinColumn(name = "task_comment_id"))
    @Column(name = "resource")
    private List<String> resources = new ArrayList<>();

    @Column(nullable = false, name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
}
