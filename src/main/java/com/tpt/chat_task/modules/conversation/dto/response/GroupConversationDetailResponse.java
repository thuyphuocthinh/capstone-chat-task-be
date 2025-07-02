package com.tpt.chat_task.modules.conversation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GroupConversationDetailResponse {
    private String id;
    private String name;
    private boolean isPinned;
    private String type;
    private MessageResponse message;
}
