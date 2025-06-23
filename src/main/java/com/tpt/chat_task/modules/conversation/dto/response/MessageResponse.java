package com.tpt.chat_task.modules.conversation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class MessageResponse {
    private String id;
    private String conversationId;
    private String senderId;
    private List<MessageElementResponse> elements;
    private boolean isPinned;
    private boolean isRead;
    // files
    // reacts
    // userReplyIds
}
