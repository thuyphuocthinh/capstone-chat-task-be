package com.tpt.chat_task.modules.conversation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PrivateConversationDetailResponse {
    private String id;
    private boolean isPinned;
    private String name;
    private String type;
    private List<ConversationMemberResponse> members;
    private MessageResponse message;
    private int countUnread;
}
