package com.tpt.chat_task.modules.conversation.dto.response;

import com.tpt.chat_task.modules.resource.enums.RESOURCE_TYPE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MessageResourceResponse {
    private String id;
    private String url;
    private String name;
    private LocalDateTime createdAt;
    private RESOURCE_TYPE resourceType;
}
