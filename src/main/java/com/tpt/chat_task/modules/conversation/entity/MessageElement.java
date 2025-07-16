package com.tpt.chat_task.modules.conversation.entity;

import com.tpt.chat_task.modules.conversation.enums.MESSAGE_ELEMENT_STYLE;
import com.tpt.chat_task.modules.conversation.enums.MESSAGE_ELEMENT_TYPE;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_elements")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Slf4j
public class MessageElement {
    @Id
    // @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(length = 36, name = "parent_id")
    private String parentId;

    @Column(name = "indent")
    @Min(value = 0, message = "Message indent cannot be negative")
    private int indent = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "style", length = 20)
    private MESSAGE_ELEMENT_STYLE style;

    @Column(name = "is_bold")
    private boolean isBold = false;

    @Column(name = "is_italic")
    private boolean isItalic = false;

    @Column(name = "is_underline")
    private boolean isUnderline = false;

    @Column(name = "content", columnDefinition = "TEXT")
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

    @Column(nullable = false, name = "order_index")
    private int orderIndex = 0;

    public void setContent(String content) {
        log.info("set content message elements: {}", content);
        this.content = content;
    }
}
