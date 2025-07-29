package com.tpt.chat_task.modules.conversation.dto.response;

import com.tpt.chat_task.modules.conversation.enums.MESSAGE_ELEMENT_TYPE;

import java.time.LocalDateTime;

public interface SearchMessageProjection {
    String getId();
    String getConversationId();
    String getHighlight();
    String getContent();
    Integer getIndent();
    MESSAGE_ELEMENT_TYPE getType(); // Hoặc dùng `MESSAGE_ELEMENT_TYPE` nếu enum map được
    Boolean getIsBold();
    Boolean getIsItalic();
    Boolean getIsUnderline();
    LocalDateTime getCreatedAt();
}
