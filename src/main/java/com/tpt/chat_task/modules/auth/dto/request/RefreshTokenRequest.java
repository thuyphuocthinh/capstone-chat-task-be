package com.tpt.chat_task.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh token cannot be null")
    @Size(max = 512, message = "Refresh token length max is 512")
    private String refreshToken;
}
