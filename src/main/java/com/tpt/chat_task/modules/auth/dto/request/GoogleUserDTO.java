package com.tpt.chat_task.modules.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoogleUserDTO {
    private String sub;
    private String givenName;
    private String familyName;
    private String email;
}
