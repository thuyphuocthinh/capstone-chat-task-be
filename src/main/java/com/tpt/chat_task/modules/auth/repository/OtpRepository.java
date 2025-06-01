package com.tpt.chat_task.modules.auth.repository;

import com.tpt.chat_task.modules.auth.entity.Otp;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRepository extends JpaRepository<Otp, String> {
    Otp findByOtp(@NotBlank(message = "OTP cannot be blank") String otp);
}
