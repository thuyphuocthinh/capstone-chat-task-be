package com.tpt.chat_task.modules.resource.entity;

import com.tpt.chat_task.modules.conversation.entity.Message;
import com.tpt.chat_task.modules.resource.enums.RESOURCE_TYPE;
import com.tpt.chat_task.modules.task.entity.Task;
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
@Table(name = "resources")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(name = "link", length = 512)
    private String link;

    @Column(name = "name", length = 512)
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Resource type cannot be null")
    @Column(name = "type", nullable = false, length = 10)
    private RESOURCE_TYPE type;

    @Column(nullable = false, name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "resources")
    private List<Message> messages = new ArrayList<>();

    @ManyToMany(mappedBy = "resources")
    private List<Task> tasks = new ArrayList<>();
}
