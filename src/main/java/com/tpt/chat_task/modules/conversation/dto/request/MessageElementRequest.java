package com.tpt.chat_task.modules.conversation.dto.request;

import com.tpt.chat_task.modules.conversation.enums.MESSAGE_ELEMENT_STYLE;
import com.tpt.chat_task.modules.conversation.enums.MESSAGE_ELEMENT_TYPE;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MessageElementRequest {
    @NotNull(message = "Message element type cannot be null")
    private MESSAGE_ELEMENT_TYPE type;

    @NotBlank(message = "Message element indent cannot be blank")
    @Min(value = 0, message = "Indent min value is 0")
    private int indent;

    private MESSAGE_ELEMENT_STYLE style;

    @NotEmpty(message = "Message elements cannot be empty")
    private List<MessageElementSectionRequest> elements;
}
