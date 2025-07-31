package com.tpt.chat_task.modules.auth.dto.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ForgotPasswordRequest {
    @Email(message = "Email is invalid")
    private String email;
}
