package com.tpt.chat_task.infrastructure.rabbitmq.dto.conversation;

import com.tpt.chat_task.modules.conversation.enums.CONVERSATION_TYPE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ConversationMemberRequest {
    private String conversationId;
    private String userId;
    private List<String> userIds;
    private CONVERSATION_TYPE type;
}
