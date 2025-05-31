package com.tpt.chat_task.modules.auth.entity;

import com.tpt.chat_task.modules.user.entity.User;
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
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tokens")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Refresh token cannot be null")
    @Column(nullable = false, unique = true, name = "refresh_token", length = 512)
    private String refreshToken;

    @Column(nullable = false, name = "revoked")
    private boolean revoked = false;

    @Column(nullable = false, length = 30, name = "ip_address")
    private String ipAddress;

    @Column(nullable = false, name = "user_agent")
    private String userAgent;

    @Column(nullable = false, name = "expired_at")
    private LocalDateTime expiredAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false, name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = true, name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}