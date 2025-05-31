package com.tpt.chat_task.modules.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ResetPasswordRequest {
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z]).{8,}$", message = "Password must be at least 8 characters long and include at least one lowercase and one uppercase letter")
    private String password;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z]).{8,}$", message = "Password must be at least 8 characters long and include at least one lowercase and one uppercase letter")
    private String confirmPassword;

    @Email(message = "Email is invalid")
    private String email;
}
