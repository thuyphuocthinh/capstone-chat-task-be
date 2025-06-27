package com.tpt.chat_task.modules.conversation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tpt.chat_task.modules.conversation.enums.MESSAGE_ELEMENT_TYPE;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class MessageElementSectionResponse extends MessageElementResponse {
    private List<MessageElementContentResponse> elements;
}
