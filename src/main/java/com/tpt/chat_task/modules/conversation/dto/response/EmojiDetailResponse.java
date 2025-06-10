package com.tpt.chat_task.modules.conversation.dto.response;

import com.tpt.chat_task.modules.conversation.enums.ICON_TYPE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class EmojiDetailResponse {
    private String id;
    private String name;
    private ICON_TYPE type;
}
