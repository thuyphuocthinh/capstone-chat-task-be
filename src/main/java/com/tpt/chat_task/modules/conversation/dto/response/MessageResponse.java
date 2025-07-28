package com.tpt.chat_task.modules.conversation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private List<MessageResourceResponse> files;
    private List<MessageReactResponse> reactions;
    private LocalDateTime createdAt;
    private int countReplies = 0;
    private List<String> userReplyIds = new ArrayList<>();
}
