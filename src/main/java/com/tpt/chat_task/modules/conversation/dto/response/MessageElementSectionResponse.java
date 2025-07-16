package com.tpt.chat_task.modules.conversation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class MessageElementSectionResponse extends MessageElementResponse {
    private List<MessageElementContentResponse> contentElements;
}
