package com.tpt.chat_task.modules.conversation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CreatePrivateConversationRequest {
    @NotEmpty(message = "User ids list cannot be empty")
    private List<String> userIds;
}
