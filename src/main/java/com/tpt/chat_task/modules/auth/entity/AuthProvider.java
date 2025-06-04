package com.tpt.chat_task.modules.auth.entity;

import com.tpt.chat_task.modules.auth.enums.AUTH_PROVIDER;
import com.tpt.chat_task.modules.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "auth_providers")
public class AuthProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "VARCHAR(36)", updatable = false, nullable = false)
    private String id;

    @Column(nullable = false, name = "provider_id")
    @NotBlank(message = "Provider id cannot be blank")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "provider", length = 10)
    @NotNull(message = "Auth provider cannot be null")
    private AUTH_PROVIDER authProvider = AUTH_PROVIDER.GOOGLE;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true, updatable = false)
    private User user;

    @Column(nullable = false, name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = true, name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (authProvider == null) {
            authProvider = AUTH_PROVIDER.GOOGLE;
        }
    }
}