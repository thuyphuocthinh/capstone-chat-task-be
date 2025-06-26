package com.tpt.chat_task.modules.conversation.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MessageReactResponse {
    private String id;
    private String name;
    private int count;
    private List<String> userIds = new ArrayList<>();
}
