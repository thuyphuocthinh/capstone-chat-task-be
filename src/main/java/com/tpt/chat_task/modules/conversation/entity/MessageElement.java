package com.tpt.chat_task.modules.conversation.entity;

import com.tpt.chat_task.modules.conversation.enums.MESSAGE_ELEMENT_TYPE;
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

@Entity
@Table(name = "message_elements")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MessageElement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

     @Column(length = 36, nullable = false, name = "parent_id")
     private String parentId;

    @Column(name = "indent")
    @Min(value = 0, message = "Message indent cannot be negative")
    private int indent;

    @Enumerated(EnumType.STRING)
    @Column(name = "style", length = 20)
    private MESSAGE_ELEMENT_TYPE style;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false, name = "content")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @NotNull(message = "Message element type cannot be null")
    private MESSAGE_ELEMENT_TYPE type;

    @Column(nullable = false, name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;
}
