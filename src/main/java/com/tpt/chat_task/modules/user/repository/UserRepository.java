package com.tpt.chat_task.modules.user.repository;

import com.tpt.chat_task.modules.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(@Email(message = "Email is invalid") @Size(min = 10, max = 255, message = "Email is too long") String email);
}
