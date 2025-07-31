package com.tpt.chat_task.modules.task.entity;

import com.tpt.chat_task.modules.workspace.entity.Workspace;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "task_boards")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TaskBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(name = "title", nullable = false)
    @NotBlank(message = "Task board title cannot be blank")
    private String title;

    @Column(name = "background_url", nullable = false, length = 512)
    @NotBlank(message = "Background url cannot be blank")
    private String backgroundUrl;

    @Column(nullable = false, name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "taskBoard")
    private List<TaskGroup> taskGroups = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;
}
