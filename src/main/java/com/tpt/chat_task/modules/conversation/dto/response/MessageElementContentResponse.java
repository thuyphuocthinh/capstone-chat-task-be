package com.tpt.chat_task.modules.conversation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tpt.chat_task.modules.conversation.enums.MESSAGE_ELEMENT_TYPE;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class MessageElementContentResponse extends MessageElementResponse {
    private String content;
}
