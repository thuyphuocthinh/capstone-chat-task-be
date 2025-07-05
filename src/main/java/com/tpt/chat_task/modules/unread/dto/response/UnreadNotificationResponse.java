package com.tpt.chat_task.modules.unread.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UnreadNotificationResponse {
    private int countUnreadAll;
    private int countUnreadMention;
    private int countUnreadReact;
    private int countUnreadActivities;
}
