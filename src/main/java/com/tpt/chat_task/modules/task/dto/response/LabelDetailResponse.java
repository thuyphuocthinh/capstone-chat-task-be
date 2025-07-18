package com.tpt.chat_task.modules.task.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class LabelDetailResponse {
    private String id;
    private String title;
    private String color;
}
