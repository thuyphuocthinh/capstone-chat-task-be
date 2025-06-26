package com.tpt.chat_task.modules.conversation.dto.request;

import com.tpt.chat_task.modules.conversation.enums.MESSAGE_ELEMENT_STYLE;
import com.tpt.chat_task.modules.conversation.enums.MESSAGE_ELEMENT_TYPE;
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
    private MESSAGE_ELEMENT_TYPE type;

    private int indent;

    private MESSAGE_ELEMENT_STYLE style;

    private List<MessageElementSectionRequest> elements;
}
