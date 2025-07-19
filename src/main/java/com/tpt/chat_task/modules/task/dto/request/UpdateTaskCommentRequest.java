package com.tpt.chat_task.modules.task.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UpdateTaskCommentRequest {
    private String content;
    private List<String> resourceLinks;
    private List<String> mentions;
}
