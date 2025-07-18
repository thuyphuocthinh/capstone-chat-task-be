package com.tpt.chat_task.modules.task.entity;

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
import java.util.UUID;

@Entity
@Table(name = "tasks")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false, name = "description")
    private String description;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(nullable = false, name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "task_group_id", nullable = false)
    private TaskGroup taskGroup;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            joinColumns = @JoinColumn(name = "task_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "label_id", nullable = false),
            name = "task_labels"
    )
    private List<Label> labels = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            joinColumns = @JoinColumn(name = "task_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "user_id", nullable = false),
            name = "task_users"
    )
    private List<User> users = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "task")
    private List<CheckList> checklists = new ArrayList<>();

    @OneToMany(mappedBy = "task", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    private List<TaskComment> taskComments = new ArrayList<>();
}
