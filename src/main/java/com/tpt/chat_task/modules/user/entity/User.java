package com.tpt.chat_task.modules.user.entity;

import com.tpt.chat_task.modules.auth.entity.Otp;
import com.tpt.chat_task.modules.auth.entity.Token;
import com.tpt.chat_task.modules.conversation.entity.Conversation;
import com.tpt.chat_task.modules.conversation.entity.Message;
import com.tpt.chat_task.modules.task.entity.Task;
import com.tpt.chat_task.modules.user.enums.USER_ROLE;
import com.tpt.chat_task.modules.user.enums.USER_STATUS;
import com.tpt.chat_task.modules.workspace.entity.WorkspaceUser;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, name = "email")
    @Email(message = "Email is invalid")
    @Size(min = 10, max = 255, message = "Email is too long")
    private String email;

    @Column(nullable = false, name = "first_name")
    @NotBlank(message = "First name cannot be blank")
    @Size(min = 1, max = 255, message = "First name length is invalid")
    private String firstName;

    @Column(nullable = false, name = "last_name")
    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 1, max = 255, message = "Last name length is invalid")
    private String lastName;

    @Column(name = "password")
    private String password;

    @Column(name = "avatar", length = 512)
    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status", length = 10)
    @NotNull(message = "User status cannot be null")
    private USER_STATUS status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "role", length = 10)
    @NotNull(message = "User role cannot be null")
    private USER_ROLE role;

    @Column(nullable = false, name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = USER_STATUS.PENDING;
        }

        if (role == null) {
            role = USER_ROLE.MEMBER;
        }
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user")
    private List<Token> tokenList = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<WorkspaceUser> workspaceUsers = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Message> messages = new ArrayList<>();

    @ManyToMany(mappedBy = "users")
    private List<Conversation> conversations = new ArrayList<>();

    @ManyToMany(mappedBy = "users")
    private List<Task> tasks = new ArrayList<>();
}
