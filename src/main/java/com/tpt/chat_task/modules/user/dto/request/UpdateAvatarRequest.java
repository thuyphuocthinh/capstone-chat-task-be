package com.tpt.chat_task.modules.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class UpdateAvatarRequest {
    @NotNull(message = "Avatar cannot be null")
    private MultipartFile avatar;
}
