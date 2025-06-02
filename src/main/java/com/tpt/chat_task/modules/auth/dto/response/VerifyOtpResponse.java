package com.tpt.chat_task.modules.auth.dto.response;

import lombok.*;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class VerifyOtpResponse {
    private String email;
}
