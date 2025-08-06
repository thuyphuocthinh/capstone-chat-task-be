package com.tpt.chat_task.modules.task.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UpdateTaskBoardRequest {
    private String title;

    private String backgroundImageUrl;
}
