package com.tpt.chat_task.modules.conversation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tpt.chat_task.modules.conversation.enums.MESSAGE_ELEMENT_STYLE;
import com.tpt.chat_task.modules.conversation.enums.MESSAGE_ELEMENT_TYPE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageElementResponse {
    private MESSAGE_ELEMENT_TYPE type;
    private int indent;
    private MESSAGE_ELEMENT_STYLE style;
    private List<MessageElementSectionResponse> elements;
}
