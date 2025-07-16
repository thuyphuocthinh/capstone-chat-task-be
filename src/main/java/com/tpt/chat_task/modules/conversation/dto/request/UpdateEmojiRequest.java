package com.tpt.chat_task.modules.conversation.dto.request;

import com.tpt.chat_task.modules.conversation.enums.ICON_TYPE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class UpdateEmojiRequest {
    private String name;

    private ICON_TYPE type;
}
