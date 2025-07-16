package com.tpt.chat_task.modules.conversation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ConversationMemberResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String avatar;
    private String roleInConversation;
}
