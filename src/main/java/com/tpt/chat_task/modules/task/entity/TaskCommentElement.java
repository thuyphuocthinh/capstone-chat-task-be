package com.tpt.chat_task.modules.task.entity;

import com.tpt.chat_task.modules.task.enums.TASK_COMMENT_ELEMENT_TYPE;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

//@Entity
//@Table(name = "task_comment_elements")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TaskCommentElement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(length = 36, nullable = false, name = "parent_id")
    private String parentId;

    @Column(name = "indent")
    @Min(value = 0, message = "Message indent cannot be negative")
    private int indent;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false, name = "content")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @NotNull(message = "Task comment element type cannot be null")
    private TASK_COMMENT_ELEMENT_TYPE type;

    @Column(nullable = false, name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "task_comment_id", nullable = false)
    private TaskComment taskComment;
}
