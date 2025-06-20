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
    private String title;
    private List<ConversationMemberResponse> members;
}
