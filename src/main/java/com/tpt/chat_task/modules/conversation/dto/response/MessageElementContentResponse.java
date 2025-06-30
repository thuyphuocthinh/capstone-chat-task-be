package com.tpt.chat_task.modules.conversation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class MessageElementContentResponse extends MessageElementResponse {
    private String content;
}
