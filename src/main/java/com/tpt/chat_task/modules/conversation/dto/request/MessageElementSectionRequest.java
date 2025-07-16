package com.tpt.chat_task.modules.conversation.dto.request;

import com.tpt.chat_task.common.annotation.ValidEnum;
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
    @ValidEnum(enumClass = MESSAGE_ELEMENT_TYPE.class)
    private MESSAGE_ELEMENT_TYPE type;

    private String content;

    private boolean isBold;

    private boolean isItalic;

    private boolean isUnderline;

    @NotEmpty(message = "Message element section cannot be empty")
    private List<MessageElementContentRequest> elements;
}
