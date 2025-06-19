package com.tpt.chat_task.modules.conversation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PublicConversationDetailResponse {
    private String id;
    private String name;
    private boolean isPinned;
    private String type;
}
