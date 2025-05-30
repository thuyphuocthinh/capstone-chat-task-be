package com.tpt.chat_task.modules.auth.repository;

import com.tpt.chat_task.modules.auth.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRepository extends JpaRepository<Otp, String> {
}
