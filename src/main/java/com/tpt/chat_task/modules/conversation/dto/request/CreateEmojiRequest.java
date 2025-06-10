package com.tpt.chat_task.modules.conversation.dto.request;

import com.tpt.chat_task.modules.conversation.enums.ICON_TYPE;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateEmojiRequest {
    @NotBlank(message = "Icon name cannot be blank")
    private String name;

    @NotNull(message = "Icon type cannot be null")
    private ICON_TYPE type;
}
