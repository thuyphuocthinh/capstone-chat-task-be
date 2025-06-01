package com.tpt.chat_task.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LogoutRequest {
    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;

    @NotBlank(message = "Access token cannot be blank")
    private String accessToken;
}
