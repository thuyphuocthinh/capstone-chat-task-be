package com.tpt.chat_task.modules.conversation.dto.response;

import com.tpt.chat_task.modules.conversation.enums.MESSAGE_ELEMENT_TYPE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SearchMessageResponse {
    private String id;
    private String conversationId;
    private String highlight;
    private String content;
    private int indent;
    private MESSAGE_ELEMENT_TYPE type;
    private boolean isBold;
    private boolean isItalic;
    private boolean isUnderline;
    private LocalDateTime createdAt;
}
