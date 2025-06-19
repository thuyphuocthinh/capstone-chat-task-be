package com.tpt.chat_task.modules.conversation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CreatePublicConversationRequest {
    @NotBlank(message = "Conversation name cannot be blank")
    @Size(max = 255, message = "Conversation name max length is 255")
    @Size(min = 1, message = "Conversation name min length is 1")
    public String name;
}
