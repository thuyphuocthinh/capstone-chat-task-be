package com.tpt.chat_task.modules.task.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class TaskBoardDetailResponse {
    private String id;
    private String title;
    private String backgroundImageUrl;
}
