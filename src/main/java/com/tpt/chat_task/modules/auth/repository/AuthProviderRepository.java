package com.tpt.chat_task.modules.auth.repository;

import com.tpt.chat_task.modules.auth.entity.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthProviderRepository extends JpaRepository<AuthProvider, String> {
}
