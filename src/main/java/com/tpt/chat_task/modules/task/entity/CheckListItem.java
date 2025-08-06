package com.tpt.chat_task.modules.task.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "checklist_items")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CheckListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(name = "title", nullable = false)
    @NotBlank(message = "Label title cannot be blank")
    private String title;

    @Column(name = "is_done", nullable = false)
    private boolean isDone = false;

    @Column(name = "order_index", nullable = false)
    private int orderIndex = 1;

    @Column(nullable = false, name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "checklist_id", nullable = false)
    private CheckList checklist;
}
