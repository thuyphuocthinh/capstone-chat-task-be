package com.tpt.chat_task.modules.task.entity;

import com.tpt.chat_task.modules.task.enums.TASK_COMMENT_TYPE;
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
    @Column(length = 36, updatable = false, nullable = false)
    private UUID id;

    @Column(length = 36, name = "thread_root_id")
    private String threadRootId;

    @Column(length = 36, name = "parent_id")
    private String parentId;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Message type cannot be null")
    @Column(name = "type", nullable = false, length = 20)
    private TASK_COMMENT_TYPE type;

    @Column(nullable = false, name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @OneToMany(mappedBy = "taskComment", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<TaskCommentElement> taskCommentElements = new ArrayList<>();
}
