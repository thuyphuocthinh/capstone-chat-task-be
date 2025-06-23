package com.tpt.chat_task.modules.conversation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tpt.chat_task.modules.conversation.enums.MESSAGE_ELEMENT_TYPE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class MessageElementContentResponse {
    private String content;
    private MESSAGE_ELEMENT_TYPE type;
}
