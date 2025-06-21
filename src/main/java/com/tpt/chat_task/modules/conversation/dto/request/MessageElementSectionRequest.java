package com.tpt.chat_task.modules.conversation.dto.request;

import com.tpt.chat_task.modules.conversation.enums.MESSAGE_ELEMENT_TYPE;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageElementSectionRequest {
    @NotNull(message = "Message element type cannot be null")
    private MESSAGE_ELEMENT_TYPE type;

    @NotEmpty(message = "Message element section cannot be empty")
    private List<MessageElementContentRequest> elements;
}
