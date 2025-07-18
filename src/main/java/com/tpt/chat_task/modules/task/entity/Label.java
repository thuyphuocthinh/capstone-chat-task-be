package com.tpt.chat_task.modules.task.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "labels")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Label {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(name = "title", nullable = false)
    @NotBlank(message = "Label title cannot be blank")
    private String title;

    @Column(name = "color", nullable = false, length = 10)
    @NotBlank(message = "Label color cannot be blank")
    @Size(max = 10, message = "Label color max lenght is 10")
    private String color;

    @Column(nullable = false, name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "labels")
    private List<Task> tasks = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "task_board_id", nullable = false)
    private TaskBoard taskBoard;
}
