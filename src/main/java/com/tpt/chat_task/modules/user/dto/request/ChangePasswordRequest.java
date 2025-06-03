package com.tpt.chat_task.modules.user.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class ChangePasswordRequest {
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z]).{8,}$", message = "Password must be at least 8 characters long and include at least one lowercase and one uppercase letter")
    private String currentPassword;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z]).{8,}$", message = "Password must be at least 8 characters long and include at least one lowercase and one uppercase letter")
    private String newPassword;

    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z]).{8,}$", message = "Password must be at least 8 characters long and include at least one lowercase and one uppercase letter")
    private String confirmPassword;
}
