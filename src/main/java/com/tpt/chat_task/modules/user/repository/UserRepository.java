package com.tpt.chat_task.modules.user.repository;

import com.tpt.chat_task.modules.user.entity.User;
import com.tpt.chat_task.modules.user.enums.USER_STATUS;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(@Email(message = "Email is invalid") @Size(min = 10, max = 255, message = "Email is too long") String email);

    Page<User> findAllByStatus(@NotNull(message = "User status cannot be null") USER_STATUS status, Pageable pageable);
}
